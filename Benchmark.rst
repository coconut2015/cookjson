.. raw:: html

    <script type="text/javascript" src="_static/echarts.min.js"></script>
	<script type="text/javascript" src="_static/vintage.js"></script>
	<script type="text/javascript">
		var options;
		var labelRight;
	</script>


Benchmarks
==========

``cookjson-benchmark`` contains some benchmark tests that compare CookJson
against GlassFish implementation, Jackson and Bson4Jackson.

Clone, Build and Run the Benchmarks 
-----------------------------------

Basically, run the following sequence of commands:

.. code-block:: bash

	git clone https://github.com/coconut2015/cookjson.git
	cd cookjson
	mvn clean package
	cd cookjson-benchmark
	java -jar target/benchmarks.jar

Here are the results on my Lenovo Thinkpad E550. In the following charts,
shorter bars indicate better performance.

Charts
------

.. raw:: html

	<div id="benchmarkChart" style="width: 600px;height:300px;"></div>
	<script type="text/javascript">
		var benchmarkChart = echarts.init(document.getElementById('benchmarkChart'));
		options = {
			tooltip : {
				trigger: 'axis',
				axisPointer : {
					type : 'shadow'
				}
			},
			legend: {
				data:['CookJson','GlassFish','Jackson','JacksonNoncanonical']
			},
			grid: {
				left: '3%',
				right: '4%',
				bottom: '3%',
				containLabel: true
			},
			xAxis : [
				{
					type : 'category',
					data : ['JSON InputStream','Json Reader','Json Generator','BSON Input'],
					splitLine: {
						show : false
					},
					splitArea: {
						show : true
					}
				}
			],
			yAxis : [
				{
					type : 'value',
					name: "ms / op",
					splitLine: {
						show : false
					},
					splitArea: {
						show : false
					}
				}
			],
			series : [
				{
					name:'CookJson',
					type:'bar',
					data:[4.918, 6.415, 30.564, 8.568]
				},
				{
					name:'GlassFish',
					type:'bar',
					data:[13.992, 8.003, 30.223, ]
				},
				{
					name:'Jackson',
					type:'bar',
					data:[4.997, 7.326, 0, 13.845]
				},
				{
					name:'JacksonNoncanonical',
					type:'bar',
					data:[12.668, 7.263, 0, ]
				}
			]
		};
		benchmarkChart.setOption(options);
	</script>
