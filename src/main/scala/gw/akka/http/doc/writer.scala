package gw.akka.http.doc

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Paths

object writer {
  def write(settings: RestDocSettings, name: String, document: Document): Unit = {
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

