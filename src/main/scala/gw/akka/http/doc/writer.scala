package gw.akka.http.doc

import java.io.{BufferedWriter, File, FileWriter}

import gw.akka.http.doc.document.Document

object writer {

  trait Writer {
    val outputDirectory: File

    def write(document: Document): Unit = {
      val documentDirectory = new File(outputDirectory, document.details.path)

      if (!documentDirectory.exists()) {
        documentDirectory.mkdirs()
      }

      val writer = new BufferedWriter(new FileWriter(new File(documentDirectory, document.details.file)))
      try {
        writer.write(document.content)
      } finally {
        writer.close()
      }
    }
  }

  object Writer {
    def apply(directory: String) = new Writer {
      override val outputDirectory = new File(directory)
    }
  }

}
