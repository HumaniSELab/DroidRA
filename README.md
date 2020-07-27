# DroidRA

Android developers heavily use reflection in their apps for legitimate reasons, but also significantly for hiding malicious actions. Unfortunately, current state-of-the-art static anal- ysis tools for Android are challenged by the presence of re- flective calls which they usually ignore. Thus, the results of their security analysis, e.g., for private data leaks, are incon- sistent given the measures taken by malware writers to elude static detection. We propose the DroidRA instrumentation- based approach to address this issue in a non-invasive way. With DroidRA, we reduce the resolution of reflective calls to a composite constant propagation problem, which we model in the Constant Propagation Language (COAL). Once the COAL solver infers the values of reflection targets in an app, we instrument it to include the corresponding tradi- tional Java call for each reflective call. Our approach allows to boost an app so that it can be immediately analyzable, including by such static analyzers that were not reflection- aware. We evaluate DroidRA on benchmark apps as well as on real-world apps, and demonstrate that it can allow state-of-the-art tools to provide more sound and complete analysis results.

## News
[2016-05-11] DroidRA has been made open source.

[2016-04-17] DroidRA paper is accepted to the 2016 International Symposium on Software Testing and Analysis (ISSTA'16), Li Li, Tegawendé F. Bissyandé, Damien Octeau and Jacques Klein, DroidRA: Taming Reflection to Support Whole-Program Analysis of Android Apps [[bib]](http://lilicoding.github.io/bibs/li2016droidra.html)

## Approach

Our work is directed toward a twofold aim: (1) to resolve reflective call targets in order to expose
all program behaviours, especially for analysis that must track private data; (2) to unbreak
app control-flow in the presence of reflective calls in order to allow static analyzers to produce additional results.  
Thus, we propose to automatically instrument Android apps in a way that Android state-of-the-art  static analyzers are able to analyze the app even in the presence of reflection. This instrumentation should produce an equivalent app whose analysis would be more sound and more complete.

The following figure presents an overview of the architecture of the DroidRA approach involving three modules. 
(1) The first module named JPM prepares the Android app to be inspected.
(2) The second module named RAM spots reflective calls and retrieves the values 
of their associated parameters (i.e., class/method/field names).
(3) Based on this information, the last module, named BOM, instruments the app and 
transforms it in a new app where reflective calls are augmented with standard java calls.  

![DroidRA Overview](images/fig_approach_overview.png)

## Benchmark Apps

In order to evaluate our approach on the support of existing static analyzers, e.g., FlowDroid or IccTA,
we provide 13 test cases (4 is avaliable in the current DroidBench project) to assess.
Excepting the four DroidBench apps, the remaining apps are currently under the benchmark-apps directory.

## Setup
The following is required to set up DroidRA:
* MAC system

##### Step 1: Load dependencies to your local repository
* git clone git@github.com:MobileSE/DroidRA.git
* cd DroidRA
* ./res/loadDependencies.sh

##### Step 2: build package：
mvn clean install

##### Step 3: example of running DroidRA(3 parameters):
* Three parameters are needed here: [your_apk_path.apk],[path of android.jar],[false]
* specific Example:
~/your_apk_path.apk
~/android-platforms/android-17/android.jar
false
