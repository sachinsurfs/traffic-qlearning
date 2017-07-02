var json2csv = require('json2csv')
var fs = require('fs')

var path = "./wednesday/"
var files = fs.readdirSync(path)
var mapping = []	// [{'road_name': <road_name>, 'jam_factor': <jam_factor>}]


files.forEach(function(file) {
	var map_data = require(path+file)
	processData(map_data, file)

} );

writeToFile();

function getAllIndexes(arr, val) {
    var indexes = [], i = -1;
    while ((i = arr.indexOf(val, i+1)) != -1){
        indexes.push(i);
    }
    return indexes;
}

function getIndex(arr1, arr2, pc, qd) {
    var indexes = [], i = -1;
		indexes = getAllIndexes(arr1,pc);
		var iii=-1;
		if(indexes.length != 0) {
			indexes.forEach(function(ii) {

				if(arr2[ii] === qd ) {
					iii=ii;
				}
			});
		}
		return iii;
}

function processData(data, file) {
	// console.log(data)
	var road_list = data['RWS'][0]['RW']
	var hmap = new Map();

	road_list.forEach(function(roads) {
		//console.log(roads)
		roads['FIS'][0]['FI'].forEach(function(road) {
			//console.log(road)
/*
Cubbon Road (B,C, H)
Queens Road (A)
St Marks Road (E)
Mg Road/Mahatma Gandhi Road (D)


Kasturba Road (F , G)
Dr. Ambedkar Road/Dr. B. S. Road/Netaji Road
Dr. Ambedkar Road (temp) (F , G)
var roadnames = ['Queens Road','Cubbon Road','Cubbon Road', 'Mg Road/Mahatma Gandhi Road', 'St Marks Road', 'Kasturba Road', 'Kasturba Road', 'Cubbon Road']


Queens Road/Minsk Squre Circle = 1916 (-) -A
Cubbon Road - 8720 (-) -B
Cubbon Road 2358 (+) - C
Cubbon Road 2096 (-) -H
Mg Road/Mahatma Gandhi Road 613 (-) - D
St Marks Road - 2542
Kasturba Road - 1434 (-)
Kasturba Road - 1434 (+)

*/
			var pccodes = [1916,8720,2358,2096,613,2542,1434,1434]

			//Some roads are merged and hence number of positive not equal to negative
			var dir = ['-','-','+','-','-','-','-','+']
			//var roadnames = ['Queens Road/Minsk Squre Circle','Cubbon Road','Cubbon Road', 'Mg Road/Mahatma Gandhi Road', 'St Marks Road', 'Kasturba Road', 'Kasturba Road', 'Cubbon Road']

			var index = getIndex(pccodes, dir, road['TMC']['PC'], road['TMC']['QD']);
			if(index!=-1) {
				hmap.set(index, road['CF'][0]['JF']);
			//var obj = {'road_name': road['TMC']['DE'], 'jam_factor': road['CF'][0]['JF']}
			//mapping.push(obj)
		}
		})
});

var tmp = file.split(".");
var filename = tmp[0];

var clock = timestamp(filename);
var obj = {'Time':clock,'A':hmap.get(0),'B':hmap.get(1),'C':hmap.get(2),'D':hmap.get(3),'E':hmap.get(4),'F':hmap.get(5),'G':hmap.get(6),'H':hmap.get(7)}
mapping.push(obj)

}


function writeToFile() {
	var csv = json2csv({ data: mapping, fields: ['Time','A','B','C','D','E','F','G','H'] });

	fs.writeFile('dailytime.csv', csv, function(err) {
		if (err) throw err;
		console.log('file saved dailytime.csv');
});
}


function timestamp(ud) {

var	date = new Date(parseInt(ud) * 1000);
// Hours part from the timestamp
var hours = date.getHours();
// Minutes part from the timestamp
var minutes = "0" + date.getMinutes();
// Seconds part from the timestamp
var seconds = "0" + date.getSeconds();

// Will display time in 10:30:23 format
var formattedTime = hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
return formattedTime;
}
