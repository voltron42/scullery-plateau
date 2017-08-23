function load() {
    var letter = document.getElementById("letter").value;
    var out = document.getElementById("output");
    var value = JSON.parse(out.value)[letter];
    loadGrid(3,1,value);
}

function encodeLetter() {
    var out = document.getElementById("output");
    var value = JSON.parse(out.value);
    var letter = document.getElementById("letter").value;
    value[letter] = encodebitmap(3, 1);
    out.value = JSON.stringify(value);
}

function togglePixel(x, y) {
    var rect = document.getElementById(x + "" + y);
    var href = rect.attributes.getNamedItem("href");
    var toggleFill = {"#r0": "#r1", "#r1": "#r0"};
    href.value = toggleFill[href.value];
}

function startup() {
    var coeff = 10;
    var squares = buildGrid(3, coeff);
    var startingColors = ["white", "black"];
    build(document.getElementsByTagName("body")[0],[{
        tag: "button",
        attrs: {onClick: "updateColors(2)"},
        children: ["Update Colors"]
    }, {
        tag: "ol",
        children: [{
            tag: "li",
            children: [{
                tag: "input",
                attrs: {
                    id: "color0",
                    type: "text",
                    value: "white"
                }
            }]
        },{
            tag: "li",
            children: [{
                tag: "input",
                attrs: {
                    id: "color1",
                    type: "text",
                    value: "black"
                }
            }]
        }]
    }, {
        tag: "button",
        attrs: {onClick: "load()"},
        children: ["Load"]
    }, {
        tag: "button",
        attrs: {onClick: "clearAll(3)"},
        children: ["Clear"]
    }, {
        tag: "button",
        attrs: {onClick: "encodeLetter()"},
        children: ["Encode"]
    }, {
        tag: "input",
        attrs: {
            id: "letter",
            type: "text",
            value: "a"
        }
    }, {
        tag: "br"
    }, {
        tag: "textarea",
        attrs: {id: "output"},
        children:["{}"]
    }, {
        tag: "br"
    }, {
        tag: "button",
        attrs: {onClick: "transform('horiz',3)"},
        children: ["Flip Horizontal"]
    }, {
        tag: "button",
        attrs: {onClick: "transform('vert',3)"},
        children: ["Flip Vertical"]
    }, {
        tag: "button",
        attrs: {onClick: "transform('right',3)"},
        children: ["Rotate Right"]
    }, {
        tag: "button",
        attrs: {onClick: "transform('left',3)"},
        children: ["Rotate Left"]
    }, {
        tag: "br"
    }, {
        tag: "svg",
        attrs: {
            width: "50%",
            height: "50%",
            viewBox: [0, 0, 8 * coeff, 8 * coeff].join(" ")
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