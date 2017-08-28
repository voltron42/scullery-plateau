function startup() {
  build(document.getElementsByTagName("body")[0],[{
    tag: "form",
    attrs: {
      action: "/api/svg/png",
      method: "post",
      target: "_blank"
    },
    children: [{
      tag: "textarea",
      attrs: {
          name: "svg",
          rows: 10,
          cols: 40
      }
    },{ tag: "br" },{
      tag: "input",
      attrs: {
        type: "submit",
          value: "Submit"
      }
    }]
  }]);
}
