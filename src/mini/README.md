CS4300-Project1- Mini Search Engine
===================================

To run this project, set up the project as described in Problem Set 2, with the src and data folders set up correctly. Depends on Java 8 and Lucene.
Then, set up the run configurations for the EvaluateQueriesMini class by going to Run -> Run Configurations. Input options as described below and run.

```run [docs cacm|med|all] [index] [tfidf atcatc|atnatn|annbpn|custom|all #|total verbose|normal] [bm25 #|total verbose|normal]```

+ `run`- required 1st keyword
+ `docs`- required keyword. After docs, the next word must be the document collection you want to consider for the rest of the arguments- cacm for CACM, med for Medlar or all for both
+ `index`- optional keyword. Run if you want to create a new index from the documents. Must be included the first time that the project is run.
+ `tfidf`- optional keyword. If included, word after must be variant to be run- atcatc, atnatn, annbpn, custom (which runs apnapn), or all. Second word after must be number of documents to retrieve, or total for all of the documents in the collection. Third word after must be verbose to output detailed information to a log file (currently at 'data/verbose_log.txt') or normal to skip this. 
+ `bm25`- optional keyword. If included, word after must be number of documents to retrieve, or total for all of the documents in the collection. Second word after must be verbose to output detailed information to a log file (currently at 'data/verbose_log.txt') or normal to skip this. 

So, for example, to run on both document sets, index the documents, retrieve based on all tfidf measures with 100 documents retrieved and no verbose logging, and retrieve based on bm25 with no verbose logging (i.e., the default setup described in questions 3 and 4), run
```run docs all index tfidf all 100 normal bm25 100 normal```

**NOTE:** verbose logging creates a very, very, VERY large file (at least 15 Gb for 100 documents retrieved) and should not be run unless you want to perform detailed failure analysis with the data. It outputs the term weights for each document for each query, depending on which similarity measure you use, the overall MAP values for each similarity metric, and the relevant documents retrieved and not retrieved. 

**NOTE:** the tfidf evaluation is currently somewhat slow, especially for CACM; be patient when running it.
