package com.jonlatane.beatpad.output.service

object Base24ConversionMap: Map<Int, List<Int>> by mapOf(
  1 to listOf(0),
  2 to listOf(0,12),
  3 to listOf(0,8,16),
  4 to listOf(0,6,12,18),
  5 to listOf(0,5,10,14,19),
  6 to listOf(0,4,8,12,16,20),
  7 to listOf(0,3,7,10,14,17,21),
  8 to listOf(0,3,6,9,12,15,18,21),
  9 to listOf(0,3,5, 8,11,13, 16,19,21),
  10 to listOf(0,2,5,7,10,12,14,17,19,22),
  11 to listOf(0,2,4,7,9,11,13,15,17,19,22),
  12 to listOf(0,2,4,6,8,10,12,14,16,18,20,22),
  13 to listOf(0,2,4,5,7,9,11,13),
  14 to (0..23).toList() - listOf(2,4,7,9,11,13,15,17,19,22),
  15 to (0..23).toList() - listOf(2,5,7,10,12,14,17,19,22),
  16 to listOf(0,2,3,5,6,8,9,11, 12,14,15,17,18,20,21,23),
  17 to (0..23).toList() - listOf(3,6,9,12,15,18,21),
  18 to listOf(0,1,3,4,5,6, 8,9,11,13, 16,19,21),
  19 to (0..23).toList() - listOf(4,8,12,16,20),
  20 to (0..23).toList() - listOf(5,10,14,19),
  21 to (0..23).toList() - listOf(6,12,18),
  22 to (0..23).toList() - listOf(8, 16),
  23 to (0..23).toList() - listOf(12),
  24 to (0..23).toList()
)