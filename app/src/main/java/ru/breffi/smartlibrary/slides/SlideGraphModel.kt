package ru.breffi.smartlibrary.slides

class SlideGraphModel(var name : String, var imageUrl : String){
    override fun equals(other: Any?): Boolean {
        return name == (other as SlideGraphModel).name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + imageUrl.hashCode()
        return result
    }
}