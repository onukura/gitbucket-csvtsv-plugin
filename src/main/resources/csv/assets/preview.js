function fixWidth () {
    const element = document.getElementsByClassName("content body clearfix");
    const prv = document.getElementById("preview");
    prv.style.width = String(element[0].offsetWidth - 40) + "px"
}
window.onload = fixWidth;