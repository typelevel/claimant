package claimant.diff

class Matrix private (cells: Array[Int], width: Int, height: Int) {

  def apply(col: Int, row: Int): Int =
    cells(row * width + col)

  def update(col: Int, row: Int, value: Int): Unit =
    cells(row * width + col) = value

  override def toString: String = {
    val pad = this(width - 1, height - 1).toString.length
    val fmt = s"%${pad}d "
    val sb = new StringBuilder
    var row = 0
    while (row < height) {
      sb.append("| ")
      var col = 0
      while (col < width) {
        sb.append(fmt.format(cells(row * width + col)))
        col += 1
      }
      sb.append("|\n")
      row += 1
    }
    sb.toString
  }
}

object Matrix {
  def empty(w: Int, h: Int): Matrix =
    new Matrix(new Array[Int](w * h), w, h)
}
