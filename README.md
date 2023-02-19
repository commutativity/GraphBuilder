# GraphBuilder

<img src="demo/images/graph-builder.png" width="200"/>  <br />

<img src="demo/images/Logo_HSAA.png" width="240"/> 

The repository provides the source code for the Graph Builder Application which has been developed for the Masterthesis "Generating Graph Datasets: Conceptualization of a Graph Builder for the Wikipedia Encyclopaedia". 

The application provides an approach to convert datasets from the Wikipedia encyclopaedia into graph datasets which can then be imported into graph exploration software e.g. Gephi. 

## Demo

The demo folder provides GEXF graph datasets that have been constructed with the application. The graph datasets can be visualized and explored in Gephi. https://gephi.org/users/download/


<img src="demo/images/main.png?raw=true" width="500"/>  <br />

<img src="demo/images/export.png?raw=true" width="500"/>


## Requirements 

The requirements for the GraphBuilder application are:

* Hadoop 3.3.1 
* Most requirements are set by the build-sbt
* The application has been run on 12 cores with 16 GB RAM
* At least 150 GB of disk space is required for the datasets from the encyclopaedia