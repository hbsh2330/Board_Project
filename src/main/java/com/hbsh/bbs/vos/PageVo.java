package com.hbsh.bbs.vos;

public class PageVo {
    public final int countPerPage; // 한 페이지당 보여줄 게시글 개수
    public final int totalCount; // 해당 게시판의 전체 게시글 수
    public final int minPage = 1; // 이동할 수 있는 가장 작은 페이지 번호
    public final int maxPage; // 이동 할 수 있는 가장 큰 페이지 번호
    public final int requestPage; //클라이언트가 요청한 페이지 번호
    public final int pageButtonCount; // 표시할 페이지 버튼 개수
    public final int queryOffset; //쿼리에서 사용할 OFFSET 값
    public final int pageFrom; // 표시할 가장 작은 페이지 번호
    public final int pageTo; // 표시할 가장 큰 페이지 번호

    public PageVo(int requestPage, int countPerPage, int pageButtonCount, int totalCount) {

        this.countPerPage = countPerPage;
        this.totalCount = totalCount;
        this.maxPage = (this.totalCount / this.countPerPage) + (this.totalCount % this.countPerPage == 0 ? 0 : 1);
        requestPage = Math.max(requestPage, this.minPage);
        requestPage = Math.min(requestPage, this.maxPage);
        this.pageButtonCount = pageButtonCount;
        this.requestPage = requestPage;
        this.pageFrom = ((this.requestPage -1) / this.pageButtonCount) * this.pageButtonCount + 1;
        this.pageTo = Math.min(this.maxPage, this.pageFrom + this.pageButtonCount -1);
        this.queryOffset = this.countPerPage * (this.requestPage - 1);
    }
}
