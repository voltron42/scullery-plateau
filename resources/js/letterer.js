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
    build(document.getElementById("canvas"),[{
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