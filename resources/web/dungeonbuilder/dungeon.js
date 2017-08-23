function publish() {
    // TODO
}

function startup() {
    build(document.getElementsByTagName("body")[0], [{
        tag: "button",
        attrs: { onClick: "updateColors(4)" },
        children: ["Update Colors"]
    },{
        tag: "ol",
        children: [{
            tag: "li",
            children: [{
                tag: "input",
                attrs: { id: "color0", type: "text" }
            }]
        },{
            tag: "li",
            children: [{
                tag: "input",
                attrs: { id: "color1", type: "text" }
            }]
        },{
            tag: "li",
            children: [{
                tag: "input",
                attrs: { id: "color2", type: "text" }
            }]
        },{
            tag: "li",
            children: [{
                tag: "input",
                attrs: { id: "color3", type: "text" }
            }]
        }]
    },{
        tag: "label",
        children: ["Tile spec:"]
    },{
        tag: "br"
    },{
        tag: "textarea",
        attrs: { id: "spec" }
    },{
        tag: "br"
    },{
        tag: "button",
        attrs: { onClick: "publish()" },
        children: ["Publish"]
    },{
        tag: "br"
    },{
        tag: "div",
        attrs: { id: "out" }
    }])
}