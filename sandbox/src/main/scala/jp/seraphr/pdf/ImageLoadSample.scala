package jp.seraphr.pdf

import java.io.FileInputStream
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectForm
import org.apache.pdfbox.util.PDFStreamEngine
import org.apache.pdfbox.util.ResourceLoader
import org.apache.pdfbox.util.PDFOperator
import java.util.{ List => JList }
import org.apache.pdfbox.cos.COSBase
import org.apache.pdfbox.cos.COSName
import scala.collection.mutable.ListBuffer
import org.apache.pdfbox.util.Matrix

object ImageLoadSample {

  def main(args: Array[String]): Unit = {
    import scala.collection.JavaConverters._

    val tFile = "FruitBasket_17.pdf"

    val tStream = new FileInputStream("src/main/resources/" + tFile)
    val tParser = new PDFParser(tStream)

    // parse内部でストリームはcloseされてる模様
    tParser.parse()

    val tPdf = tParser.getPDDocument()
    val tPages = tPdf.getDocumentCatalog().getAllPages().asScala.toSeq.asInstanceOf[Seq[PDPage]]

    val tImages = tPages.flatMap { tPage =>
      new MyStreamEngine map { tEngine =>
        tEngine.processStream(tPage, tPage.findResources(), tPage.getContents().getStream())
        tEngine.result
      }
    }

    //    val tImages = tPages.flatMap { tPage =>
    //      val tObjects = tPage.getResources().getXObjects().asScala.values
    //      tObjects.collect {
    //        case tImage: PDXObjectImage => Iterable(tImage)
    //        case tForm: PDXObjectForm => tForm.getResources().getImages().asScala.values
    //      }.flatten
    //    }

    val tImageCount = tImages.size
    println(s"tImages size = ${tImageCount}")
    tImages.take(10).zipWithIndex.foreach {
      case ((tImage, tDpi), tIndex) =>
        println(s"${tIndex}/${tImageCount} ${tDpi}")

        tImage.write2file(f"out/${tFile}-${tIndex}%03d")
    }

    println("owata")
  }

  case class Dpi(x: Double, y: Double)

  class MyStreamEngine extends PDFStreamEngine(
    ResourceLoader.loadProperties("org/apache/pdfbox/resources/PDFTextStripper.properties", true)) {

    private val mListBuffer = new ListBuffer[(PDXObjectImage, Dpi)]

    def result = mListBuffer.toList

    def calcDpi(aCtm: Matrix, aPageHeight: Double, aImage: PDXObjectImage): Dpi = {
      val tYScale = aCtm.getYScale()
      val tImageWidth = aImage.getWidth()
      val tImageHeight = aImage.getHeight()

      val tAngleCoefficient =
        if (aCtm.getValue(0, 1) < 0 && aCtm.getValue(1, 0) > 0)
          -1
        else
          1

      val angle = tAngleCoefficient * Math.acos(aCtm.getValue(0, 0) / aCtm.getXScale());
      println(s"angle = ${angle}  yScale=${tYScale}")

      aCtm.setValue(2, 1, (aPageHeight - aCtm.getYPosition() - Math.cos(angle) * tYScale).asInstanceOf[Float]);
      aCtm.setValue(2, 0, (aCtm.getXPosition() - Math.sin(angle) * tYScale).asInstanceOf[Float]);
      aCtm.setValue(0, 1, (-1) * aCtm.getValue(0, 1));
      aCtm.setValue(1, 0, (-1) * aCtm.getValue(1, 0));

      val ctmAT = aCtm.createAffineTransform();
      ctmAT.scale(1f / tImageWidth, 1f / tImageHeight);

      println(aCtm)

      val imageXScale = aCtm.getXScale();
      val imageYScale = aCtm.getYScale();

      val (tWidthInch, tHeightInch) = (imageXScale / 72, imageYScale / 72)

      Dpi(tImageWidth / tWidthInch, tImageHeight / tHeightInch)
    }

    override def processOperator(operator: PDFOperator, arguments: JList[COSBase]): Unit = {
      val operation = operator.getOperation();
      val tInvokeOperator = "Do"

      if (operation != tInvokeOperator) {
        super.processOperator(operator, arguments)
      } else {
        val objectName = arguments.get(0).asInstanceOf[COSName]
        val xobjects = getResources().getXObjects()
        val xobject = xobjects.get(objectName.getName())

        mListBuffer += {
          xobject match {
            case image: PDXObjectImage =>
              val page = getCurrentPage()
              val width = image.getWidth()
              val height = image.getHeight()
              val ctmNew = getGraphicsState().getCurrentTransformationMatrix()

              calcDpi(ctmNew, page.getMediaBox().getHeight(), image)

              val tCropBox = page.findCropBox()

//              val tDpi = Dpi(width * 72 / tCropBox.getWidth(), height * 72 / tCropBox.getHeight())
              val tDpi = calcDpi(ctmNew, page.getMediaBox().getHeight(), image)

              (image, tDpi)
          }
        }
      }
    }
  }

  implicit class Tapper[A](obj: A) {
    def tap(f: A => Unit): A = { Option(obj).foreach(f); obj }
    def map[B](f: A => B): B = { Option(obj).map(f).get }
  }
}