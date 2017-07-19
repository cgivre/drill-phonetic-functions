# Phonetic and Distance Functions for Apache Drill
This repository contains a series of phonetic functions to be used with Apache Drill.  These functions are based on the algorithms found in org.apache.commons.codec.language.  This package includes Soundex, Metaphone and Double Metaphone algorithms.

## Usage

### Soundex
The package includes an implementation of the Soundex algorithm which you can read about [here](https://en.wikipedia.org/wiki/Soundex).  Per wikipedia, Soundex is a phonetic algorithm for indexing names by sound, as pronounced in English. The goal is for homophones to be encoded to the same representation so that they can be matched despite minor differences in spelling. The algorithm mainly encodes consonants; a vowel will not be encoded unless it is the first letter. Soundex is the most widely known of all phonetic algorithms (in part because it is a standard feature of popular database software such as DB2, PostgreSQL, MySQL, Ingres, MS SQL Server and Oracle) and is often used (incorrectly) as a synonym for "phonetic algorithm". Improvements to Soundex are the basis for many modern phonetic algorithms.

To obtain the soundex for a word, simply call the soundex function as shown below:
```
SELECT soundex( <field> ) FROM <file>;
```

### Sounds Like
There is also a `sounds_like( string1, string2 )` method which compares the soundex values of two strings and returns true if they are equal, false if not.  This is most useful in the `WHERE` clause of a Drill query where you can use it to find data similar to a known value.  For instance:

```
jdbc:drill:zk=local> select * FROM dfs.drilldev.`names.csv` WHERE sounds_like( columns[0], 'Jayme' );
+--------------------+
|      columns       |
+--------------------+
| ["Jaime","jayme"]  |
+--------------------+
1 row selected (0.263 seconds)
```
You can see the full example of everything in the query below.

```
jdbc:drill:zk=local> select columns[0] as name1,
columns[1] as name2,
soundex( columns[0] ) AS soundex_1,
soundex( columns[1] ) AS soundex_2,
sounds_like( columns[0], columns[1] ) AS sounds_like
FROM dfs.drilldev.`names.csv`;
=======
jdbc:drill:zk=local> select columns[0] as name1, 
columns[1] as name2, 
soundex( columns[0] ) AS soundex_1, 
soundex( columns[1] ) AS soundex_2, 
sounds_like( columns[0], columns[1] ) AS sounds_like  
FROM dfs.drilldev.`names.csv`; 
>>>>>>> d6671b0a26a204ca90430a01fea3703826cb013c
+----------------+-----------------+------------+------------+--------------+
|     name1      |      name2      | soundex_1  | soundex_2  | sounds_like  |
+----------------+-----------------+------------+------------+--------------+
| charles        | bob             | C642       | B100       | false        |
| There          | their           | T600       | T600       | true         |
| their          | there           | T600       | T600       | true         |
| they're        | they            | T600       | T000       | false        |
| Jaime          | jayme           | J500       | J500       | true         |
| Mohammad       | Muhammad        | M530       | M530       | true         |
| Charles Givre  | Alisheva Givre  | C642       | A421       | false        |
+----------------+-----------------+------------+------------+--------------+
7 rows selected (0.228 seconds)
```

## Metaphone
This package includes an implementation of the Metaphone algorithm which you can read about [here](https://en.wikipedia.org/wiki/Metaphone).  Per wikipedia, Metaphone is a phonetic algorithm, published by Lawrence Philips in 1990, for indexing words by their English pronunciation.[1] It fundamentally improves on the Soundex algorithm by using information about variations and inconsistencies in English spelling and pronunciation to produce a more accurate encoding, which does a better job of matching words and names which sound similar. As with Soundex, similar-sounding words should share the same keys.

To obtain the metaphone value of a word,
```sql
SELECT metaphone( 'jayme' ) AS metaphone
FROM (values(1));
+------------+
| metaphone  |
+------------+
| JM         |
+------------+
```

## Double Metaphone


## How to Compile and Install

Clone and compile.

```

git clone https://github.com/cgivre/drill-phonetic-function

cd drill-phonetic-function

mvn package

```

Copy the jar files from your functions into the 3rdparty directory in the Drill distro

```
cp drill-phonetic-function/target/*.jar apache-drill-<version>/jars/3rdparty
```

Now run drill and test the results

```
$ cd apache-drill-1.8.0/
$ bin/drill-embedded
0: jdbc:drill:zk=local>
SELECT soundex( first_name ) AS soundex from cp.`employee.json` limit 5;
+----------+
| soundex  |
+----------+
| S600     |
| D620     |
| M240     |
| M000     |
| R163     |
+----------+
5 rows selected (0.534 seconds)

```
