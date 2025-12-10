package com.example.study_app.ui.Subject;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.example.study_app.network.GeminiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SubjectAdviceProvider {

    // Callback kết quả tư vấn
    public interface TuVanCallback {
        void thanhCong(String noiDung);

        void thatBai(Exception e);
    }

    public interface AdviceCallback {
        void onSuccess(String advice);

        void onError(Exception e);
    }

    // Gọi Gemini để lấy tư vấn cho môn học
    public static void layTuVanTuGemini(String tenMonHoc, @NonNull TuVanCallback callback) {
        Executor xuLyNen = Executors.newSingleThreadExecutor();
        Handler handlerChinh = new Handler(Looper.getMainLooper());

        xuLyNen.execute(() -> {
            GeminiClient client = new GeminiClient();
            String loiNhac = "Là một chuyên gia tư vấn chuyên ngành công nghệ thông tin, hãy đưa ra tư vấn ngắn gọn cho môn học có tên '"
                    + tenMonHoc
                    + "'. Nội dung tư vấn cần gồm: \n1. Kiến thức cốt lõi (những kiến thức chính sẽ học). \n2. Hướng ngành liên quan (lĩnh vực việc làm phù hợp). \nHãy trả lời tự nhiên, trực tiếp bằng tiếng Việt.";
            try {
                String phanHoiJson = client.generateContent(loiNhac);
                String noiDungTuVanTmp = phanTichPhanHoiGemini(phanHoiJson);
                final String noiDungTuVan = lamSạchMarkdownCoBan(noiDungTuVanTmp);
                handlerChinh.post(() -> callback.thanhCong(noiDungTuVan));
            } catch (Exception e) {
                final Exception loi = e;
                handlerChinh.post(() -> callback.thatBai(loi));
            }
        });
    }

    // Wrapper tương thích ngược: dùng tên hàm cũ và callback cũ
    public static void getAdviceFromGemini(String subjectName, @NonNull AdviceCallback callback) {
        layTuVanTuGemini(subjectName, new TuVanCallback() {
            @Override
            public void thanhCong(String noiDung) {
                callback.onSuccess(noiDung);
            }

            @Override
            public void thatBai(Exception e) {
                callback.onError(e);
            }
        });
    }

    // Phân tích JSON từ Gemini để trích xuất nội dung text
    private static String phanTichPhanHoiGemini(String phanHoiJson) throws Exception {
        JSONObject goc = new JSONObject(phanHoiJson);
        if (goc.has("error")) {
            String thongDiepLoi = goc.getJSONObject("error").optString("message", "Lỗi không xác định");
            throw new Exception("Lỗi từ API Gemini: " + thongDiepLoi);
        }

        JSONArray ungVien = goc.getJSONArray("candidates");
        if (ungVien.length() > 0) {
            JSONObject ungVienDauTien = ungVien.getJSONObject(0);
            JSONObject noiDung = ungVienDauTien.getJSONObject("content");
            JSONArray phan = noiDung.getJSONArray("parts");
            if (phan.length() > 0) {
                JSONObject phanDauTien = phan.getJSONObject(0);
                if (phanDauTien.has("text")) {
                    return phanDauTien.getString("text");
                }
            }
        }
        throw new Exception("Không tìm thấy nội dung tư vấn trong phản hồi.");
    }

    // Làm sạch ký hiệu Markdown cơ bản để hiển thị đẹp hơn ở TextView/Dialog
    private static String lamSạchMarkdownCoBan(String s) {
        if (s == null || s.isEmpty())
            return "";
        String kq = s;
        kq = kq.replaceAll("(?m)^#{1,6}\\s*", "");
        kq = kq.replace("**", "");
        kq = kq.replaceAll("\\*(.+?)\\*", "$1");
        kq = kq.replaceAll("(?m)^-\\s+", "• ");
        kq = kq.replaceAll("[\\t\\f\\r]", " ").replaceAll(" +", " ").trim();
        return kq;
    }
}
