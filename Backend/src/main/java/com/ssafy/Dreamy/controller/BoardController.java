package com.ssafy.Dreamy.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.Dreamy.model.BoardDto;
import com.ssafy.Dreamy.model.service.BoardService;
import com.ssafy.Dreamy.model.service.ParticipateService;


@CrossOrigin(origins = { "http://localhost:3000" })
//@CrossOrigin(origins = { "http://i4a306.p.ssafy.io" })
@RestController
@RequestMapping("/board")	// 매핑주소 변경가능
public class BoardController {
	
	public static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";
	private static final String DEFAULTIMAGEURL = "https://d2dmrocw1z3urn.cloudfront.net/board/default";

	@Autowired
	private BoardService boardService;

	@Autowired
	private ParticipateService participateService;
	
	// 검색
	@GetMapping("/search/{keyword}/{limit}")
	public ResponseEntity<Map<String, Object>> getList(@PathVariable("keyword") String EncodedKeyword, @PathVariable("limit") int limit, @RequestParam("uid") int uid, HttpServletRequest request) throws UnsupportedEncodingException {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		String Decodedkeyword = URLDecoder.decode(EncodedKeyword, "UTF-8");
		logger.info("keyword : {}", Decodedkeyword);
		
		try {
			int totalSize = boardService.searchTotalSize(Decodedkeyword);
			List<BoardDto> list = boardService.searchList(Decodedkeyword, limit);
			if (totalSize > limit) {	// 리스트가 있을 때
				resultMap.put("list", list);
				resultMap.put("totalSize", totalSize);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {					// 리스트가 없을 때
				resultMap.put("list", null);
				resultMap.put("message", FAIL);
				status = HttpStatus.NO_CONTENT;
			}
		} catch (Exception e) {
			logger.error("목록 불러오기 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	// 게시물 등록
	@PostMapping("/insert")	// 매핑주소 변경가능
	public ResponseEntity<Map<String, Object>> create(@RequestBody BoardDto boardDto) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		int boardType = boardDto.getBoardType();
		int ret = 0;
		if(boardDto.getImageUrl() == null) {
			
			switch(boardDto.getCategory()) {
			case 1:
				boardDto.setImageUrl(DEFAULTIMAGEURL + "/exercise.jpg");
				break;
			case 2:
				boardDto.setImageUrl(DEFAULTIMAGEURL + "/food.jpg");
				break;
			case 3:
				boardDto.setImageUrl(DEFAULTIMAGEURL + "/travel.jpg");
				break;
			case 4:
				boardDto.setImageUrl(DEFAULTIMAGEURL + "/study.jpg");
				break;
			case 5:
				boardDto.setImageUrl(DEFAULTIMAGEURL + "/cultureorlife.jpg");
				break;
			case 6:
				boardDto.setImageUrl(DEFAULTIMAGEURL + "/etc.jpg");
				break;
			}
		}
		
		try {
			if (boardType == 1)			// 버킷리스트
				ret = boardService.insertBucket(boardDto);
			else if (boardType == 2)	// 챌린지
				ret = boardService.insertChallenge(boardDto);
			
			// insert 성공하면 AI값을 return
			if (ret > 0) {	// 등록 성공
				if(participateService.addParticipant(boardDto.getUid(), boardDto.getPid()) > 0) { // 참가 성공
					resultMap.put("message", SUCCESS);
					status = HttpStatus.CREATED;
				}
				else {		// 참가 실패
					resultMap.put("message", FAIL);
					status = HttpStatus.EXPECTATION_FAILED;
				}
			} else {		// 등록 실패
				resultMap.put("message", FAIL);
				status = HttpStatus.EXPECTATION_FAILED;
			}
		} catch (Exception e) {
			logger.error("게시물 등록 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	// 게시물 목록 불러오기
	@GetMapping("/list/{category}/{limit}")
	public ResponseEntity<Map<String, Object>> getInfo(@PathVariable("category") int category, @PathVariable("limit") int limit,
													@RequestParam("uid") int uid, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		logger.info("게시물 목록, category : {}", category);
		try {
			int totalSize = boardService.getListTotalSize(category);	// 게시물 개수
			List<BoardDto> list = boardService.getList(uid, category, limit);
			if (totalSize > limit) {	// 리스트가 있을 때
				resultMap.put("list", list);
				resultMap.put("totalSize", totalSize);
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {					// 리스트가 없을 때
				resultMap.put("list", null);
				resultMap.put("message", FAIL);
				status = HttpStatus.NO_CONTENT;
			}
		} catch (Exception e) {
			logger.error("목록 불러오기 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	// 게시물 수정(내용만)
	@PutMapping("/update") 
	public ResponseEntity<Map<String, Object>> update(@RequestBody BoardDto boardDto) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		try {
			int pid = boardDto.getPid();
			String content = boardDto.getContent();
			boardService.update(pid, content);
			status = HttpStatus.ACCEPTED;
		}catch(Exception e) {
			logger.error("게시물 수정 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
	// 게시물 삭제
	@DeleteMapping("/delete/{pid}")
	public ResponseEntity<Map<String, Object>> deleteReply(@PathVariable("pid") int pid, HttpServletRequest request) {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		
		try {
			int ret = boardService.delete(pid);
			if (ret > 0) {
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.EXPECTATION_FAILED;
			}
		} catch (Exception e) {
			logger.error("댓글 삭제 실패 : {}", e);
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}
	
}
