cookjson-jsonpath
=================

``cookjson-jsonpath`` is an extremely simple implementation of
``com.jayway.jsonpath.spi.json.JsonProvider`` for `Jayway JsonPath <https://github.com/jayway/JsonPath>`__

Maven
-----

.. code-block:: xml

	<dependency>
		<groupId>org.yuanheng.cookjson</groupId>
		<artifactId>cookjson-jsonpath</artifactId>
		<version>1.0</version>
	</dependency>

Code Examples
-------------

Example 1: Simple Extraction
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example demonstates the basic workflow of using Jayway JsonPath together
with CookJson.

.. code-block:: java

		// Setup Jayway JsonPath
		BasicConfigurator.configure ();

		// Create a CookJson JsonProvider for JsonPath
		JsonPathProvider provider = new JsonPathProvider ();

		// Test JSON file
		File file = new File ("../tests/data/data3.json");

		Configuration pathConfig = Configuration.defaultConfiguration ().jsonProvider (provider);
		// Compile a JSON path
		JsonPath path = JsonPath.compile ("$..A");

		// Apply the JSON path to a file to extract the path
		JsonValue value = path.read (file, pathConfig);

		// use toJson function to convert the tree model to string
		System.out.println (provider.toJson (value));

Example 2: Working with BSON
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Jayway JsonPath is mainly tailored toward text based handling.  Fortunately,
it does support handling of tree models.  So it is possible to handle different
formats of JSON.

.. code-block:: java

		BasicConfigurator.configure ();

		JsonPathProvider provider = new JsonPathProvider ();

		File file = new File ("../tests/data/data1.bson");

		Configuration pathConfig = Configuration.defaultConfiguration ().jsonProvider (provider);
		JsonPath path = JsonPath.compile ("$..A");

		// Read BSON into a tree model
		JsonProvider p = new CookJsonProvider ();
		HashMap<String, Object> readConfig = new HashMap<String, Object> ();
		readConfig.put (CookJsonProvider.FORMAT, CookJsonProvider.FORMAT_BSON);
		readConfig.put (CookJsonProvider.ROOT_AS_ARRAY, Boolean.TRUE);
		JsonReaderFactory rf = p.createReaderFactory (readConfig);
		JsonReader reader = rf.createReader (new FileInputStream (file));
		JsonStructure obj = reader.read ();
		reader.close ();

		// Apply the JSON path to the tree model generated from a BSON file
		JsonValue value = path.read (obj, pathConfig);

		// use toJson function to convert the tree model to string
		System.out.println (provider.toJson (value));
