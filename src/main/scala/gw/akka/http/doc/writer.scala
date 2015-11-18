package gw.akka.http.doc

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths

import gw.akka.http.doc.RestDoc.Settings
import gw.akka.http.doc.document.Document

object writer {
  def write(settings: Settings, name: String, document: Document): Unit = {
    val documentPath = Paths.get(settings.OutputDirectory, name)
    val documentDirectory = documentPath.toFile

    if (!documentDirectory.exists()) {
      documentDirectory.mkdirs()
    }

    val writer = new BufferedWriter(new FileWriter(new File(documentDirectory, s"${document.name}.${settings.FormatterExtension}")))
    try {
      writer.write(document.content)
    } finally {
      writer.close()
    }
  }
}

