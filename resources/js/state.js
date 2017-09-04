function setAllValues(value, targets) {
    targets.forEach(function(target) {
        document.getElementById(target).value = value;
    })
}

function preLoadData(targets) {
    setAllValues(JSON.stringify(data), targets);
}

function updateState(source,targets) {
    setAllValues(document.getElementById(source).value, targets);
}

function setFilename(input) {
}

function newState() {
    location.replace("/state/test")
}