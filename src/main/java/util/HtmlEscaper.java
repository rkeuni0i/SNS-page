package util;

/**
 * 기존 main.jsp / userList.jsp는 DB에서 읽은 사용자 입력을 문자열 연결로
 * HTML에 그대로 삽입했다.
 *
 *     str += "<tr><td colspan=2>" + feed.getContent() + "</td></tr>";
 *
 * 이 경우 누군가 본문에 <script>...</script> 를 넣어 글을 쓰면, 그 글을 보는
 * 모든 사용자의 브라우저에서 스크립트가 실행된다(저장형 XSS, Stored XSS).
 * 세션 쿠키 탈취로 이어질 수 있는 실제 취약점이다.
 *
 * 화면에 출력하는 모든 사용자 입력은 이 클래스를 거치도록 한다.
 */
public final class HtmlEscaper {

    private HtmlEscaper() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /** HTML 텍스트 노드 및 따옴표로 감싼 속성값에 안전하게 삽입할 수 있도록 이스케이프한다. */
    public static String escape(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        StringBuilder sb = new StringBuilder(raw.length() + 16);
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;");  break;
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    /** 줄바꿈을 <br>로 바꾸되, 그 외 문자는 모두 이스케이프한다(게시글 본문용). */
    public static String escapeWithLineBreaks(String raw) {
        return escape(raw).replace("\r\n", "<br>").replace("\n", "<br>");
    }
}
