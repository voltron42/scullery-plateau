function publish() {
    // TODO
}

function startup() {
    build(document.getElementsByTagName("body")[0], [{
        tag: "button",
        attrs: { onClick: "updateColors(2)" },
        children: ["Update Colors"]
    },{
        tag: "br"
    },{
        tag: "label",
        children: ["Foreground:"]
    },{
        tag: "input",
        attrs: { id: "color1", type: "text" }
    },{
        tag: "br"
    },{
        tag: "label",
        children: ["Background:"]
    },{
        tag: "input",
        attrs: { id: "color0", type: "text" }
    },{
        tag: "br"
    },{
        tag: "label",
        children: ["Alphabet spec:"]
    },{
        tag: "br"
    },{
        tag: "textarea",
        attrs: { id: "alpha" }
    },{
        tag: "br"
    },{
        tag: "label",
        children: ["Message:"]
    },{
        tag: "br"
    },{
        tag: "textarea",
        attrs: { id: "msg" }
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