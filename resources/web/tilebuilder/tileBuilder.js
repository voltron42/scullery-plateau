function load() {
    var out = document.getElementById("output");
    var value = JSON.parse(out.value);
    loadGrid(4,2,value);
}

function encodeLetter() {
    console.log("You clicked the 'Encode' button.");
    var out = document.getElementById("output");
    var outbytes = encodebitmap(4, 2);
    out.innerHTML = JSON.stringify(outbytes);
}

function togglePixel(x, y) {
    console.log("You clicked the square at ("+x+","+y+")");
    var list = document.getElementsByName("pickcolor");
    var option = "toggle";
    for (var i = 0; i < list.length; i++) {
        if (list[i].checked) {
            option = list[i].value;
            break;
        }
    }
    var rect = document.getElementById(x.toString(16) + "" + y.toString(16));
    var fill = rect.attributes.getNamedItem("href");
    var toggleFill = {
        "#r0": "#r1",
        "#r1": "#r2",
        "#r2": "#r3",
        "#r3": "#r0"
    };
    if (option == "toggle") {
        fill.value = toggleFill[fill.value];
    } else {
        fill.value = option;
    }
}

function startup() {
    var coeff = 10;
    var squares = [];
    var range = [];
    for(var x = 0; x < 16; x++) {
        range.push(x);
    }
    range.forEach(function(x) {
        range.forEach(function (y) {
            squares.push({
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
            })
        });
    });
    var startingColors = ["white", "red", "green", "blue"];
    build(document.getElementsByTagName("body")[0],[{
        tag: "button",
        attrs: {onClick: "updateColors(4)"},
        children: ["Update Colors"]
    }, {
        tag: "ul",
        children:[{
            tag: "li",
            children: [{
                tag: "input",
                attrs: {
                    name: "pickcolor",
                    type: "radio",
                    checked: true,
                    value: "toggle"
                }
            }, {
                tag: "label",
                children: ["(toggle color)"]
            }]
        }]
    }, {
        tag: "ol",
        children: startingColors.map(function(color, index) {
            return {
                tag: "li",
                children: [{
                    tag: "input",
                    attrs: {
                        name: "pickcolor",
                        type: "radio",
                        value: "#r" + index
                    }
                }, {
                    tag: "input",
                    attrs: {
                        id: "color" + index,
                        type: "text",
                        value: color
                    }
                }]
            }
        })
    }, {
        tag: "button",
        attrs: {onClick: "load()"},
        children: ["Load"]
    }, {
        tag: "button",
        attrs: {onClick: "clearAll(4)"},
        children: ["Clear"]
    }, {
        tag: "button",
        attrs: {onClick: "encodeLetter()"},
        children: ["Encode"]
    }, {
        tag: "br"
    }, {
        tag: "textarea",
        attrs: {id: "output"}
    }, {
        tag: "br"
    }, {
        tag: "button",
        attrs: {onClick: "transform('horiz',4)"},
        children: ["Flip Horizontal"]
    }, {
        tag: "button",
        attrs: {onClick: "transform('vert',4)"},
        children: ["Flip Vertical"]
    }, {
        tag: "button",
        attrs: {onClick: "transform('right',4)"},
        children: ["Rotate Right"]
    }, {
        tag: "button",
        attrs: {onClick: "transform('left',4)"},
        children: ["Rotate Left"]
    }, {
        tag: "br"
    }, {
        tag: "svg",
        attrs: {
            width: "50%",
            height: "50%",
            viewBox: [0, 0, 16 * coeff, 16 * coeff].join(" ")
        },
        children: [{
            tag: "defs",
            children: startingColors.map(function(color, index) {
                return {
                    tag: "rect",
                    attrs: {
                        id: "r" + index,
                        width: coeff,
                        height: coeff,
                        fill: color
                    }
                }
            })
        }].concat(squares)
    }]);
}