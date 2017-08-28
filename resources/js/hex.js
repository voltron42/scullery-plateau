var frame = {
	tag: "polygon",
	attrs: {
		id: "frame",
		points: [
			[0,10],
			[5,0],
			[15,0],
			[20,10],
			[15,20],
			[5,20]
		].map(function(p){
			return p.join(",");
		}).join(" "),
		stroke: "black",
		"stroke-width": "1",
		fill: "inherit"
	}
};
function TileSet(appendTo,defId) {
	var mytiles = 0;
	var tileCtrl = function(n) {
		return [{
			tag: "li",
			children: [{
				tag: "input",
				attrs: {
					name: "tileSelect",
					id: "tileSelect" + n,
					type: "radio",
					checked: "true",
					value: n
				}
			},{
				tag: "ul",
				children: [{
					tag: "li",
					children: [{
						tag: "label",
						children: [" Name: "]
					},{
						tag: "input",
						attrs: {
							id: "tileName" + n,
							type: "text",
						}
					}]
				},{
					tag: "li",
					children: [{
						tag: "label",
						children: [" Color: "]
					},{
						tag: "input",
						attrs: {
							id: "tileColor" + n,
							type: "text",
						}
					}]
				},{
					tag: "li",
					children: [{
						tag: "label",
						children: [" JSON Contents: "]
					},{
						tag: "textarea",
						attrs: {
							id: "tileContents" + n,
						}
					}]
				}]
			}]
		}]
	}
	var tilePattern = function(name,color,contents) {
		return {
			tag: "g",
			attrs: {
				id: name
			},
			children: [{
				tag: "rect",
				attrs: {
					width: 20,
					height: 20,
					fill: color
				}
			}].concat(contents)
		};
	}
	this.add = function() {
		append(document.getElementById(appendTo),tileCtrl(mytiles));
		mytiles++;
	}
	this.update = function() {
		build(document.getElementById(defId),[frame].concat(range(mytiles).map(function(n){
			var fields = ["tileName","tileColor","tileContents"].map(function(label){
				return document.getElementById(label + n).value;
			})
			return tilePattern.apply(null,fields);
		})));
	}
	this.getSelected = function() {
		return range(mytiles).map(function(n){
			return document.getElementById("tileSelect" + n);
		}).filter(function(radio){
			return radio.checked;
		}).map(function(radio){
			return radio.value;
		})[0];
	}
	this.getTiles = function() {
		return range(mytiles).reduce(function(out,n){
			var tile = ["tileName","tileColor","tileContents"].reduce(function(obj,label){
				obj[label] = document.getElementById(label + n).value;
				return obj;
			},{});
			out[tile.tileName] = ["tileColor","tileContents"].reduce(function(obj,label){
				obj[label] = tile[label];
				return obj;
			},{});
			return out;
		},{});
	}
}

function Grid(instanceName,appendTo,defId,tileSet) {
	var grid = {};
	this.init = function() {
		build(document.getElementById(appendTo),[{
			tag: "svg",
			attrs: {
				id: "mySvg",
				width: "50%",
				height: "50%",
				viewBox: "0 0 0 0"
			},
			children: [{
				tag: "defs",
				attrs: {
					id: "defs"
				},
				children:[frame]
			},{
				tag: "g",
				attrs: {
					id: "thisGrid"
				}
			}]
		}]);
	}
	this.setDim = function(widthField, heightField) {
		for (var x in grid) {
			delete(grid[x]);
		}
		var width = document.getElementById(widthField).value;
		var height = document.getElementById(heightField).value;
		var svg = document.getElementById("mySvg");
		var viewBox = svg.attributes.getNamedItem("viewBox");
		var size = {
			width: width * 15 + 7,
			height: height * 20 + 12
		};
		viewBox.value = [0,0,size.width,size.height].join(" ");
		var hexes = range(height).reduce(function(out,row){
			return range(width).reduce(function(prev,col){
				var id = col + "-" + row;
				return prev.concat({
					tag: "use",
					attrs: {
						id: id,
						x: 1 + 15 * col,
						y: 1 + 20 * row + ((col % 2) * 10),
						href: "#frame",
						fill: "white",
						onClick: instanceName + ".applyTile(" + col + "," + row + ")"
					}
				})
			},out);
		},[{
			tag: "rect",
			attrs: {
				width: size.width,
				height: size.height,
				stroke: "black",
				"stroke-width": "1",
				fill: "white"
			}
		}]);
		build(document.getElementById("thisGrid"), hexes);
	}
	this.publish = function(textOut) {
		var output = document.getElementById(textOut);
		var json = {
			tiles:tileSet.getTiles(),
			map:Object.keys(grid).reduce(function(out,key){
				var tileId = grid[key];
				var point = key.split("-").map(parseInt);
				if (tileId in out) {
					out[tileId].push(point);
				} else {
					out[tileId] = [point];
				}
				return out;
			},{})
		};
		output.innerHTML = JSON.stringify(json);
	}
	this.applyTile = function(col,row) {
		tileSet.update();
		console.log(JSON.stringify(tileSet.getTiles()));
		var tileIndex = tileSet.getSelected();
		console.log("tile index: " + tileIndex);
		var tileId = document.getElementById("tileName" + tileIndex).value;
		console.log("tile id: " + tileId);
		var hexId = col + "-" + row;
		console.log("hex id: " + hexId);
		grid[hexId] = tileId;
		console.log("grid: " + JSON.stringify(grid));
		var hex = document.getElementById(hexId);
		var fill = hex.attributes.getNamedItem("fill");
		fill.value = "url(#" + tileId + ")";
	}
}

var tiles = new TileSet("input-list","defs");

var myGrid = new Grid("myGrid","map","defs",tiles);

function startup() {
	build(document.getElementsByTagName("body")[0],[{
		tag: "UL",
		attrs: {
			id: "input-list"
		}
	},{
		tag: "br"
	},{
		tag: "button",
		attrs:{
			onClick: "tiles.add()"
		},
		children: ["Add Tile"]
	},{
		tag: "button",
		attrs:{
			onClick: "tiles.update()"
		},
		children: ["Update Tiles"]
	},{
		tag: "br"
	},{
		tag: "label",
		children: ["Width: "]
	},{
		tag: "input",
		attrs: {
			id: "cols",
			type: "number",
			style: "width: 5em",
			value: 1
		}
	},{
		tag: "label",
		children: [" Height: "]
	},{
		tag: "input",
		attrs: {
			id: "rows",
			type: "number",
			style: "width: 5em",
			value: 1
		}
	},{
		tag: "button",
		attrs:{
			onClick: "myGrid.setDim('cols','rows')"
		},
		children: ["Build"]
	},{
		tag: "br"
	},{
		tag: "button",
		attrs:{
			onClick: "myGrid.publish('output')"
		},
		children: ["Publish"]
	},{
		tag: "br"
	},{
		tag: "div",
		attrs: {
			id: "map"
		}
	},{
		tag: "br"
	},{
		tag: "textarea",
		attrs: {
			id: "output"
		}
	}]);
	myGrid.init();
}