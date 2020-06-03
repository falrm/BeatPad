package com.jonlatane.beatpad.view.palette.filemanagement

//class FooTest {
//  companion object {
fun compareStrings(str1: String, str2: String) =
  str1.foldIndexed<Int?>(null) { index, acc, char ->
    when {
      acc != null        -> acc
//      index > str2.length -> 1
      char < str2[index] -> -1
      char > str2[index] -> 1
      else            -> null
    } ?: when {
      str2.length == str1.length -> 0
      str2.length > str1.length  -> 1
      else                       -> -1
    }

  }

fun main() {
  println(compareStrings("abce", "abc"))
}
//  }
//}