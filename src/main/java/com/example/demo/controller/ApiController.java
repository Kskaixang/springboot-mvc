package com.example.demo.controller;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.BMI;
import com.example.demo.model.Book;
import com.example.demo.responce.ApiResponse;

//@RestController免去撰寫 @ResponseBody 但若要透過JSP 渲染 則不可用
//@RequestMapping {"/api/home" , "/api/"} 變成 {"/home" , "/"}  統一省略 url前綴
@RestController
@RequestMapping("/api") //統一 url前綴
public class ApiController {
	/*
	 * 1.首頁
	 * 路徑 /api/home
	 * 路徑 /api/
	 * 網址 http://localhost:8080/api/home
	 * 網址 http://localhost:8080/api/
	 */
	@GetMapping(value = {"/home" , "/"})
	public String home() {		
		return "我是首頁";
	}
	
	/*
	 * 2. ?帶參數
	 * 路徑: /greet?name=Jogn&age=18
	 * 路徑: /greet?name=Mery
	 * 網址 http://localhost:8080/api/greet?name=Jogn&age=18
	 * 結果 Hi John , 18 (成年)
	 * 網址 http://localhost:8080/api/greet?name=Mery
	 * 結果 Hi Mery , 0 (未成年)
	 * 限制:name 必填 age可選 有初始值0
	 * 
	 * required = true 代表一定要有
	 */
	@GetMapping("/greet")
	public String greet(@RequestParam(value = "name", required = true) String username,
			            @RequestParam(value = "age", required = false,defaultValue = "0") Integer userage) {	
		String result = String.format("Hi %s %d (%s)",
				username,userage , userage > 18 ? "成年" : "未成年");
		return result;
	}
	
	//精簡寫法 方法名與請求參數名 相同
	@GetMapping("/greet2")
	public String greet2(@RequestParam String name,
			             @RequestParam(defaultValue = "0") Integer age) {	
		String result = String.format("Hi %s %d (%s)",
				name,age , age > 18 ? "成年" : "未成年");
		return result;
	}
	
	//練習
	
	/*
	 * 體重(公斤) / 身高2(公尺2)
	 */
	
	@GetMapping("/bmi")
	public String bmi(@RequestParam Integer h,
			          @RequestParam Integer w) {
	    double bmi = w / (Math.pow(h / 100.0,2) );
	    return String.format("BMI = %.2f", bmi);
	}
	//你chrom 有安裝jsonview 可以宣告這個 produces = "application/json;charset=utf-8" 會比較好看
	@GetMapping(value = "/bmi2", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<BMI>> calcBmi(@RequestParam(required = false) Double h,
	          			  @RequestParam(required = false) Double w) {
		if(h == null || w == null) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請提供身高(h)或體重(w)"));
		}
		double bmi = w / Math.pow(h/100, 2);		
		return ResponseEntity.ok(ApiResponse.success("BMI 計算成功", new BMI(h, w, bmi)));
	}
	//--------------------------------------------------
	/**
	 * 5. 同名多筆資料
	 * 路徑: /age?age=17&age=21&age=20
	 * 網址: http://localhost:8080/api/age?age=17&age=21&age=20
	 * 請計算出平均年齡
	 * */
	@GetMapping(value = "/age", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getAverage(@RequestParam("age") List<String> ages){
		//orElseGet(()->0 代表沒找到時 沒給任何數 即是0
		if(ages == null || ages.size() == 0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入年齡"));
		}
		double avg = ages.stream().mapToInt(Integer::parseInt).average().orElseGet(()->0);
		Object map = Map.of("年齡",ages, "平均年齡",String.format("%.1f",avg));
		return ResponseEntity.ok(ApiResponse.success("計算成功", map));
	}
	/*
	 * 6. Lab 練習: 得到多筆 score 資料
	 * 路徑: "/exam?score=80&score=100&score=50&score=70&score=30"
	 * 網址: http://localhost:8080/api/exam?score=80&score=100&score=50&score=70&score=30
	 * 請自行設計一個方法，此方法可以
	 * 印出: 最高分=?、最低分=?、平均=?、總分=?、及格分數列出=?、不及格分數列出=?
	 */
	@GetMapping(value = "/exam", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getExamInfo(@RequestParam(name = "score", required = false) List<Integer> scores){
		//orElseGet(()->0 代表沒找到時 沒給任何數 即是0
		if(scores == null || scores.size() == 0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("請輸入分數"));
		}
		//統計物件 此物件只接收基本型別 所以需要mapToInt(Integer::intValue)轉換
		IntSummaryStatistics stat = scores.stream().mapToInt(Integer::intValue).summaryStatistics();
		//利用 Collectors.partitioningBy 分組
		// Key=true 及格分數 | key = false 不及格
		Map<Boolean, List<Integer>> resultMap = scores.stream()
				.collect(Collectors.partitioningBy(score -> score >= 60));
		Object data = Map.of(
				"最高分",stat.getMax(),
				"最低分",stat.getMin(),
				"平均分",stat.getAverage(),
				"總分",stat.getSum(),
				"及格",resultMap.get(true),
				"不及格",resultMap.get(false));
				
		return ResponseEntity.ok(ApiResponse.success("計算成功", data));
		
	
		
	}
	/*
	 * 7. 多筆參數轉 Map
	 * name 書名(String), price 價格(Double), amount 數量(Integer), pub 出刊/停刊(Boolean)
	 * 路徑: /book?name=Math&price=12.5&amount=10&pub=true
	 * 路徑: /book?name=English&price=10.5&amount=20&pub=false
	 * 網址: http://localhost:8080/api/book?name=Math&price=12.5&amount=10&pub=true
	 * 網址: http://localhost:8080/api/book?name=English&price=10.5&amount=20&pub=false
	 * 讓參數自動轉成 key/value 的 Map 集合
	 * */
	@GetMapping(value = "/book", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getBookInfo(@RequestParam Map<String, Object> bookMap) {
		System.out.println(bookMap);
		return ResponseEntity.ok(ApiResponse.success("回應成功", bookMap));
	}
	/*
	 {
	  "message": "回應成功",
	  "data": {
	    "name": "Math",
	    "price": "12.5",
	    "amount": "10",
	    "pub": "true"
	  }
	}
	*/
	
	
	
	/*
	 * 8.多筆參數轉指定model物件  日後你可以添加進資料入啊? 陣列阿
	 * 路徑網址 同上
	 * 
	 * 上面是用 Map去接收   這邊是用 model物件去接
	 */
	@GetMapping(value = "/book2", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Object>> getBookInfo2(Book book) {
		book.setId(1);
		System.out.println(book);		
		return ResponseEntity.ok(ApiResponse.success("回應成功2", book));
	}
	/*
	 	{
		  "message": "回應成功2",
		  "data": {
		    "id": null,
		    "name": "Math",
		    "price": 12.5,
		    "amount": 10,
		    "pub": true
		  }
		}
	 */
	
	/*
	 * 9.路徑參數  網址設計 核心知識
	 * 在以前的方法 早期設計風格
	 * 路徑 : /book?id=1 得到 id = 1 的書
	 * 網址 http://localhost:8080/api/book/1
	 * 
	 * (Rest)現在的方法 不需要寫'?'  表現層狀態轉換（英語：Representational State Transfer，縮寫：REST
	 * GET /books    **查詢所有書籍
	 * GET /book/1   **查詢單筆書籍
	 * POST /book    **新增書籍
	 * PUT /book/1   **修改單筆書籍
	 * DELETE /book/1 **刪除單筆書籍  這是一種設計風格 不是死規矩
	 * 重點用於 呈現某一件商品  一件喔!
	 * 路徑 : /book/1 得到 id = 1 的書
	 * 網址 http://localhost:8080/api/book/1
	 */
	
	
	
	@GetMapping(value = "/book/{id}", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<Book>> getBookInfo3(@PathVariable(name = "id") Integer id) {
		List<Book> books = List.of(
					new Book(1,"A",12.5,20,false),
					new Book(2,"B",10.5,20,false),
					new Book(3,"C",8.5,20,true),
					new Book(4,"D",12.5,20,true)
				);
		//搜尋該筆書籍 判斷是否有找到
		Optional<Book> opBook = books.stream().filter(book -> book.getId().equals(id)).findFirst();
		if(opBook.isEmpty()) {
			return ResponseEntity.badRequest().body(ApiResponse.error("錯誤:找不到或是參數錯誤"));
		}				
		return ResponseEntity.ok(ApiResponse.success("回應成功", opBook.get()));
	}
	
	@GetMapping(value = "/book/pub/{isPub}", produces = "application/json;charset=utf-8")
	public ResponseEntity<ApiResponse<List<Book>>> queryBook(@PathVariable Boolean isPub) {
		List<Book> books = List.of(
					new Book(1,"A",12.5,20,false),
					new Book(2,"B",10.5,20,false),
					new Book(3,"C",8.5,20,true),
					new Book(4,"D",12.5,20,true)
				);
		List<Book> tureBooks = books.stream().filter(book -> book.getPub().equals(isPub)).toList();
		if(tureBooks.size() == 0) {
			return ResponseEntity.badRequest().body(ApiResponse.error("錯誤:找不到或是參數錯誤"));
		}				
		return ResponseEntity.ok(ApiResponse.success("回應成功" + (isPub ? "出刊" : "停刊"), tureBooks));
	}
}
