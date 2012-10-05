$(function() {
	
	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	
	window.chart = new Highcharts.StockChart({
		chart : {
			renderTo : 'container',
			events : {
				load : function() {
					
					var series = this.series[0];
					setInterval(
						function() {
							var x = (new Date()).getTime();
							afterSetExtremes(x - 90 * 1000, x);
						}, 
						90000
					);
				}
			}
		},
		
		title: {
			text: 'Server memory usage'
		},
		
		rangeSelector: {
			buttons: [{
				count: 1,
				type: 'minute',
				text: '1M'
			}, {
				count: 5,
				type: 'minute',
				text: '5M'
			}, {
				type: 'all',
				text: 'All'
			}],
			inputEnabled: false,
			selected: 0
		},

		exporting: {
			enabled: false
		},

		series : [{
			name : 'Memory',
			data : (function() {
				// generate an array of random data
				var data = [], time = (new Date()).getTime();
				var startTS = time - 999000;
				var endTS = time;

				$.ajax({
				    type: 'GET',
				    url: 'memory-json.xq',
				    data: {
			 			serverId: window.serverId,
			 			start: Math.round(startTS),
						end: Math.round(endTS), 
						callback: "?"
					},
				    dataType: 'json',
				    success: function(d, textStatus, jqXHR) {
						$.each(d, function(i, val) { 
							data.push([
								val[0],
								val[1]
							]);
						});
					 },
				    async: false
				});

				return data;
			})()
		}]
	});
});


function afterSetExtremes(startTS, endTS) {

	chart.showLoading('Loading data from server...');
	$.getJSON('memory-json.xq',
		{
 			serverId: window.serverId,
 			start: Math.round(startTS),
			end: Math.round(endTS), 
			callback: "?"
		},
		function(data) {
			var series = chart.series[0];

			$.each(data, function(i, item) {
				series.addPoint([item[0], item[1]], true, true);
			});

			chart.hideLoading();
		}
	);
};