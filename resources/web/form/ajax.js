function startup() {
  build(document.getElementsByTagName("body")[0],[{
    tag: "form",
    attrs: {
      action: "/api/form",
      method: "post",
      enctype: "multipart/form-data"
      target: "_blank"
    },
    children: [{
      tag: "file",
      attrs: {
          name: "file"
      }
    },{ tag: "br" },{
      tag: "input",
      attrs: {
        type: "submit",
          value: "Read File"
      }
    }]
  }]);
}
