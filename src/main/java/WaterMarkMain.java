
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.itextpdf.text.* ;

import com.itextpdf.text.pdf.*;
import org.apache.commons.io.FileUtils;

public class WaterMarkMain {

    public static void main(String[] args) throws IOException, DocumentException {
        String inPath = "C:\\watermark\\in_pdf\\";
        String outPath = "C:\\watermark\\out_pdf\\";
        File dir = new File(inPath);
        File[] fileList = dir.listFiles();
        //FileWriter writer = new FileWriter(new File("/Users/pc/test/test.txt"), true);

        for(int i=0; i<fileList.length; i++) {
            File file = fileList[i];
            System.out.println(file.getName());
            PdfCreate(file.getName(),inPath,outPath);
        }

    }

    private static void PdfCreate(String fileName, String inPath, String outPath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        // 출력할 pdf 파일
        PdfWriter writer = PdfWriter.getInstance(document, FileUtils.openOutputStream(new File(outPath + fileName)));

        document.open();

        PdfContentByte canvas = writer.getDirectContent();

        // 원본 pdf 파일(클래스패스에서 가져옴)
        //PdfReader reader = new PdfReader(WaterMarkMain.class.getResourceAsStream("./in/7002270-21-1389.pdf"));
        PdfReader reader = new PdfReader(inPath + fileName);

        // 워터마크 이미지(클래스패스에서 가져옴)
        Image img = Image.getInstance(WaterMarkMain.class.getClassLoader().getResource("watermark.jpg"));
        img.setAbsolutePosition(350, 640);
        img.scalePercent(65);

        // 원본 pdf 에서 페이지를 하나씩 읽어서 출력 pdf 에 추가한다.
        for (int num = 1, size = reader.getNumberOfPages(); num <= size; num++) {
            PdfImportedPage page = writer.getImportedPage(reader, num);

            document.newPage();

            // 0, 0 위치에 페이지 추가(참고로 itext에서 좌표 체계는 왼쪽 아래에서 부터 0, 0으로 시작함, 그래프와 비슷함)
            canvas.addTemplate(page, 0, 0);
            // 첫번째 페이지만 상단에 테이블 추가
            if (num == 1) {
                SimpleDateFormat sd = new SimpleDateFormat( "yyyy-MM-dd");
                Date date = new Date();
                String today = sd.format(date);

                // (0, -1) : 전체 row, 뒤에 숫자 두개는 추가할 위치(x, y) = 왼쪽에서 20, top+20 위치에 테이블 추가
                int xPos = 365;
                int yPos = -65;
                writeText(today).writeSelectedRows(0, -1, document.left() + xPos, document.top() + yPos, canvas);
                yPos = -97;
                String documentNumber = fileName.split("-")[2].toLowerCase(Locale.ROOT).replace(".pdf","");
                writeText(documentNumber).writeSelectedRows(0, -1, document.left() + xPos, document.top() + yPos, canvas);
                //이미지 투명하게 처리
                PdfGState state = new PdfGState();
                state.setFillOpacity(0.4f);
                canvas.setGState(state);

                // 워터마트 이미지 추가
                canvas.addImage(img);
            }
        }

        document.close();
    }



    private static PdfPTable writeText(String text) throws DocumentException, IOException {
        // 클래스패스 루트에 나눔고딕 폰트 추가(한글을 쓰기 위해서 한글폰트를 설정해 줘야됨)
        BaseFont base_font = BaseFont.createFont("NanumGothic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

        Font font = new Font(base_font, 10);
        //Font header_font = new Font(base_font, 10);
        //header_font.setColor(BaseColor.WHITE);

        // 컬럼 1개 짜리 테이블
        PdfPTable table = new PdfPTable(1);

        table.setTotalWidth(new float[] { 80 });
        table.setLockedWidth(true);

        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(0);
        table.addCell(cell);

        return table;
    }
}
