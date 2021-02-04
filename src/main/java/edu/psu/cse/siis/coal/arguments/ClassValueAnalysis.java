/*
 * Copyright (C) 2015 The Pennsylvania State University and the University of Wisconsin
 * Systems and Internet Infrastructure Security Laboratory
 *
 * Author: Damien Octeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.psu.cse.siis.coal.arguments;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lu.uni.snt.droidra.util.ApplicationClassFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JInstanceFieldRef;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.scalar.Pair;
import edu.psu.cse.siis.coal.AnalysisParameters;
import edu.psu.cse.siis.coal.Constants;

/**
 * An argument value analysis for class types.
 */
public class ClassValueAnalysis extends BackwardValueAnalysis {
  private static final String TOP_VALUE = Constants.ANY_CLASS;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Override
  public Set<Object> computeInlineArgumentValues(String[] inlineValues) {
    return new HashSet<Object>(Arrays.asList(inlineValues));
  }

  @Override
  public Object getTopValue() {
    return null;
  }

  /**
   * Returns the set of possible values of a variable of type class.
   *
   * @param value The variable whose value we are looking for.
   * @param start The statement where the analysis should start.
   * @return The set of possible values for the variable.
   */
  @Override
  public Set<Object> computeVariableValues(Value value, Stmt start) {
    if (value instanceof ClassConstant) {
      return Collections.singleton((Object) ((ClassConstant) value).getValue());
    } else if (value instanceof Local) {
      return processClassAssignments(
          findAssignmentsForLocal(start, (Local) value, true, new HashSet<Pair<Unit, Local>>()),
          new HashSet<Stmt>());
    } else {
      return Collections.singleton((Object) TOP_VALUE);
    }
  }

  private List<DefinitionStmt> handleVariableInvoke(Value rhsValue, Unit start, Set<ValueBox> visitedVariables){
    List<DefinitionStmt> result = new ArrayList<>();

    if(CollectionUtils.isNotEmpty(rhsValue.getUseBoxes())){
      for (ValueBox useBox : rhsValue.getUseBoxes()) {
        if(visitedVariables.contains(useBox)){
          continue;
        }
        visitedVariables.add(useBox);
        if(useBox.getValue() instanceof Local){
          List<DefinitionStmt> assignmentList = findAssignmentsForLocal(start, (Local) useBox.getValue(), true, new HashSet<Pair<Unit, Local>>());
          result.addAll(assignmentList);
          for(DefinitionStmt stmt : assignmentList){
            Value rop = stmt.getRightOp();
            if(CollectionUtils.isNotEmpty(rop.getUseBoxes())){
              result.addAll(handleVariableInvoke(rop, stmt, visitedVariables));
            }
          }
        }
      }
    }
    return result;
  }


  /**
   * Processes assignment to local variables that have a class type.
   *
   * @param assignStmts A list of assignment statements to a given local variable.
   * @param visitedStmts The set of statements visited by the analysis.
   * @return The set of possible value given by the assignment statements.
   */
  private Set<Object> processClassAssignments(List<DefinitionStmt> assignStmts,
      Set<Stmt> visitedStmts) {
    Set<Object> result = new HashSet<>(assignStmts.size());

    for (DefinitionStmt assignStmt : assignStmts) {
      Value rhsValue = assignStmt.getRightOp();
      if (rhsValue instanceof ClassConstant) {
        result.add(((ClassConstant) rhsValue).getValue().intern());
      } else if (rhsValue instanceof ParameterRef) {
        ParameterRef parameterRef = (ParameterRef) rhsValue;
        Iterator<Edge> edges =
            Scene.v().getCallGraph()
                .edgesInto(AnalysisParameters.v().getIcfg().getMethodOf(assignStmt));
        while (edges.hasNext()) {
          Edge edge = edges.next();
          InvokeExpr invokeExpr = edge.srcStmt().getInvokeExpr();
          Set<Object> newResults =
              computeVariableValues(invokeExpr.getArg(parameterRef.getIndex()), edge.srcStmt());

          List<Object> filterNewResults = newResults.stream().filter(ob->{ return !ob.toString().equals(TOP_VALUE) && !ob.toString().equals(Constants.NULL_STRING); }).collect(Collectors.toList());
          if(CollectionUtils.isNotEmpty(filterNewResults)){
            result.addAll(filterNewResults);
          }else {
            result.addAll(Collections.singleton((Object) TOP_VALUE));
          }
        }
      } else if (rhsValue instanceof InvokeExpr) {
        InvokeExpr invokeExpr = (InvokeExpr) rhsValue;
        SootMethod method = invokeExpr.getMethod();
        if (method.getSignature().equals(
            "<java.lang.Class: java.lang.Class forName(java.lang.String)>")
                || method.getSignature().equals(
                "<java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>")
                || method.getSignature().equals(
                "<java.lang.ClassLoader: java.lang.Class loadClass(java.lang.String)>")) {
          Set<Object> classNames = ArgumentValueManager.v()
                            .getArgumentValueAnalysis(Constants.DefaultArgumentTypes.Scalar.STRING)
                            .computeVariableValues(invokeExpr.getArg(0), assignStmt);

          Set<Object> filterClassNames = classNames.stream().filter(item->{
            return !item.toString().equals(TOP_VALUE) && !item.toString().equals(Constants.NULL_STRING);
          }).map(item2->{ return item2.toString().replace("(.*)",""); }).collect(Collectors.toSet());

          if(CollectionUtils.isNotEmpty(filterClassNames)){
            result.addAll(filterClassNames);
          }else{
            result.addAll(Collections.singleton((Object) TOP_VALUE));
          }

        } else if (method.getSignature().equals("<java.lang.Object: java.lang.Class getClass()>")) {
          VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
          if (logger.isDebugEnabled()) {
            logger.debug("Returning " + virtualInvokeExpr.getBase().getType().toString());
          }

          //Optimize DroidRA to enhance accuracy by back tracing Local value
          Set<Object> specificClassName = backTraceLocal(visitedStmts, assignStmt, virtualInvokeExpr);
          if (specificClassName != null) return specificClassName;

          return Collections.singleton((Object) virtualInvokeExpr.getBase().getType().toString());
        } else {
          Set<Object> constantClasses = handleInvokeExpression(assignStmt, visitedStmts);
          if (constantClasses == null) {
            result.addAll(Collections.singleton((Object) TOP_VALUE));
            //return Collections.singleton((Object) TOP_VALUE);
          } else {
            result.addAll(constantClasses);
          }
        }
      } else {
        result.addAll(Collections.singleton((Object) TOP_VALUE));
        //return Collections.singleton((Object) TOP_VALUE);
      }
    }

    if (result.size() == 0) {
      return Collections.singleton((Object) TOP_VALUE);
    }
    return result;
  }

  private Set<Object> backTraceLocal(Set<Stmt> visitedStmts, DefinitionStmt assignStmt, VirtualInvokeExpr virtualInvokeExpr) {
    List<DefinitionStmt> assignmentList = findAssignmentsForLocal(assignStmt,(Local)virtualInvokeExpr.getBase(), true, new HashSet<Pair<Unit, Local>>());
    //process assignStmts
    List<DefinitionStmt> enrichAssignmentList = new ArrayList<>();
    enrichAssignmentList.addAll(assignmentList);
    for (DefinitionStmt as : assignmentList) {
      Value rv = as.getRightOp();
      if (CollectionUtils.isNotEmpty(rv.getUseBoxes())) {
        List<DefinitionStmt> additionalAssignments = handleVariableInvoke(rv, as, new HashSet<>());
        enrichAssignmentList.addAll(additionalAssignments);
      }
    }

    Set<Object> objects = processClassAssignments(enrichAssignmentList, visitedStmts);

    if(CollectionUtils.isNotEmpty(objects)){
        List<Object> specificClassName = objects.stream().filter(ob->{ return !ob.toString().equals(TOP_VALUE) && !ob.toString().equals(Constants.NULL_STRING); }).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(specificClassName)){
            return Collections.singleton((Object) specificClassName.get(0).toString());
        }
    }
    return null;
  }

  /**
   * Returns the variable values that are associated with an call statement.
   *
   * @param sourceStmt The statement at which we should start.
   * @param visitedStmts The set of visited statements.
   * @return The set of possible values.
   */
  protected Set<Object> handleInvokeExpression(Stmt sourceStmt, Set<Stmt> visitedStmts) {
    if (visitedStmts.contains(sourceStmt)) {
      return Collections.emptySet();
    } else {
      visitedStmts.add(sourceStmt);
    }
    Iterator<Edge> edges = Scene.v().getCallGraph().edgesOutOf(sourceStmt);
    Set<Object> result = new HashSet<>();

    while (edges.hasNext()) {
      Edge edge = edges.next();
      SootMethod target = edge.getTgt().method();
      if (target.isConcrete()) {
        for (Unit unit : target.getActiveBody().getUnits()) {
          if (unit instanceof ReturnStmt) {
            ReturnStmt returnStmt = (ReturnStmt) unit;

            Value returnValue = returnStmt.getOp();
            if (returnValue instanceof StringConstant) {
              result.add(((StringConstant) returnValue).value);
            } else if (returnValue instanceof ClassConstant) {
              result.add(((ClassConstant) returnValue).value);
            } else if (returnValue instanceof Local) {
              List<DefinitionStmt> assignStmts =
                  findAssignmentsForLocal(returnStmt, (Local) returnValue, true,
                      new HashSet<Pair<Unit, Local>>());
              Set<Object> classConstants = processClassAssignments(assignStmts, visitedStmts);
              if (classConstants == null || classConstants.equals(TOP_VALUE)
                  || classConstants.equals(Constants.ANY_STRING)) {
                return null;
              } else {
                result.addAll(classConstants);
              }
            } else {
              return null;
            }
          }
        }
      }
    }

    return result;
  }
}
