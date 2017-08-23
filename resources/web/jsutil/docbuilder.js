
function buildNode(node) {
    var out = ["<",node.tag];
    if ((typeof node.attrs) == "object") {
        Object.keys(node.attrs).filter(function(key) {
            return (typeof node.attrs[key]) != 'undefined';
        }).forEach(function(key){
            out = [].concat(out," ",key,"=",'"',node.attrs[key],'"');
        });
    }
    out.push(">");
    if (Array.isArray(node.children) && node.children.length > 0) {
        out = out.concat(node.children.map(function(child){
            if ((typeof child) == "string") {
                return child;
            } else {
                return buildNode(child);
            }
        }));
    }
    out = out.concat("</",node.tag,">");
    return out.join("");
}

function build(appendTo,nodes) {
    appendTo.innerHTML = nodes.map(buildNode).join("");
}

function append(appendTo,nodes) {
    appendTo.innerHTML += nodes.map(buildNode).join("");
}

function updateColors(count) {
    for (var i = 0; i < count; i++) {
        var color = document.getElementById("color" + i);
        var r = document.getElementById("r" + i);
        r.attributes.getNamedItem("fill").value = color.value;
    }
}

function range(count) {
	return "x".repeat(count).split("").map(function(v,i) {
		return i;
	});
}

function applyToGrid(bitsize,func) {
    var size = (1 << bitsize);
    range(size).forEach(function(y) {
        range(size).forEach(function (x) {
            func(x, y, size);
        });
    });
}


function encodebitmap(bitsize, colorssize) {
    var colorcount = 1 << colorssize;
    var pershort = 2 << (4 - colorssize);
    var colorbits = {};
    for (var c = 0; c < colorcount; c++) {
        colorbits["#r" + c] = c;
    }
    var colors = [];
    applyToGrid(bitsize, function (x, y, size) {
        var rect = document.getElementById(x.toString(size) + "" + y.toString(size));
        var fill = rect.attributes.getNamedItem("href");
        colors.push(colorbits[fill.value]);
    });
    var outbytes = [];
    while (colors.length > 0) {
        outbytes.push(colors.splice(0, pershort));
    }
    return outbytes.map(function (shortlist) {
        return shortlist.reduce(
            function (sum, val, index) {
                return sum | (val << (colorssize * index));
            }, 0);
    });
}

function clearAll(bitsize) {
    applyToGrid(bitsize,function (x, y, size) {
        var rect = document.getElementById(x.toString(size) + "" + y.toString(size));
        var fill = rect.attributes.getNamedItem("href");
        fill.value = "#r0";
    });
}

function buildGrid(bitsize, coeff) {
    var grid = [];
    applyToGrid(bitsize, function (x, y, size) {
        grid.push({
            tag: "use",
            attrs: {
                id: x.toString(16) + "" + y.toString(16),
                x: x * coeff,
                y: y * coeff,
                stroke: "black",
                href: "#r0",
                "stroke-width": 1,
                onClick: "togglePixel("+x+","+y+")"
            }
        });
    });
    return grid;
}

function buildTypes(width,height) {
    function getX(x, y) {
        return x;
    }
    function getY(x, y) {
        return y;
    }
    function invertX(size) {
      return function(x, y) {
        return (size - 1) - x;
      }
    }
    function invertY(size) {
      return function(x, y) {
        return (size - 1) - y;
      }
    }
    return {
        horiz: {
            x: invertX(width),
            y: getY
        },
        vert: {
            x: getX,
            y: invertY(height)
        },
        right: {
            x: invertY(width),
            y: getX
        },
        left: {
            x: getY,
            y: invertX(height)
        }
    }
}

function transform(type, bitsize) {
    var size = (1 << bitsize);
    var tf = buildTypes(size,size)[type];
    var newPoints = [];
    applyToGrid(bitsize, function (x, y, size) {
        var rect = document.getElementById(x.toString(size) + "" + y.toString(size));
        var fill = rect.attributes.getNamedItem("href");
        newPoints.push({
            x: tf.x(x, y),
            y: tf.y(x, y),
            fill: fill.value
        })
    });
    var size = 1 << bitsize;
    newPoints.forEach(function (p) {
        var rect = document.getElementById(p.x.toString(size) + "" + p.y.toString(size));
        var fill = rect.attributes.getNamedItem("href");
        fill.value = p.fill
    })
}

function tfGrid(type,width,height) {
  var grid = range(height).reduce(function(out,y){
    return range(width).reduce(function(next,x){
      //TODO
    },out);
  },{});
}

function loadGrid(bitsize, colorsize, shortlist) {
    var size = 1 << bitsize;
    var colorCount = 1 << colorsize;
    var colorlist = shortlist.reduce(function (out, short) {
        for (var b = 0; b < bitsize; b++) {
            out.push((short >>> b) % colorCount);
        }
    }, []);
    colorlist.forEach(function (color, index) {
        var x = index % size;
        var y = Math.floor(index / size);
        var rect = document.getElementById(x.toString(size) + "" + y.toString(size));
        var fill = rect.attributes.getNamedItem("href");
        fill.value = "#r" + color;
    })
}

function button(label,fnCall) {
  return {
    tag: "button",
    attrs: { onClick: fnCall },
    children: [label]
  };
}

function radio(radioGroup,id,value,checked) {
  var attrs = {
    type: "radio",
    name: radioGroup,
    id: id,
    value: value
  }
  if (checked) {
    attrs.checked = true;
  }
  return {
    tag: "input",
    attrs: attrs
  }
}

function number(id,value,onChange) {
  var attrs = {
    id: id,
    type: "number",
    value: value,
    style: "width: 5em;",
  }
  if (onChange) {
    attrs.onChange = onChange;
  }
  return {
    tag: "input",
    attrs: attrs
  }
}

function labeled(name,label,item) {
  item.attrs.name = name;
  return [{
    tag: "label",
    attrs:{ for: name },
    children: [label]
  }].concat(item);
}

function listed() {
  return {
    tag: "li",
    children: arguments
  }
}

function text(id,value,onChange) {
  var attrs = {id:id,type:"text"};
  if (value) {
    attrs.value = value;
  }
  if (onChange) {
    attrs.onChange = onChange;
  }
  return {tag:"input",attrs:attrs};
}

function br() {
  return { tag: "br" };
}
