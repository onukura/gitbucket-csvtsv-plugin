function fixWidth () {
    var element = document.getElementsByClassName("content body clearfix")
    var prv = document.getElementById("preview")
    prv.style.width = String(element[0].offsetWidth*0.95) + "px"
}
window.onload = fixWidth();