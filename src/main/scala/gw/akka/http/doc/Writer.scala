package gw.akka.http.doc

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.{Path, Paths}

import gw.akka.http.doc.document.Document

private[doc] trait Writer {
  val outputDirectory: String

  def write(document: Document, testDirectory: String, extension: String): Unit = {
    val documentPath = Paths.get(outputDirectory, testDirectory)
    val documentDirectory = documentPath.toFile

    if (!documentDirectory.exists()) {
      documentDirectory.mkdirs()
    }

    val writer = new BufferedWriter(new FileWriter(new File(documentDirectory, s"${document.name}.$extension")))
    try {
      writer.write(document.content)
    } finally {
      writer.close()
    }
  }
}

private[doc] object Writer {
  def apply(directory: String) = new Writer {
    override val outputDirectory = directory
  }
}
