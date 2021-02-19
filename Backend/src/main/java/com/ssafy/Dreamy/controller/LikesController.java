package com.ssafy.Dreamy.controller;

import java.util.HashMap;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.Dreamy.model.LikesDto;
import com.ssafy.Dreamy.model.service.LikesService;

@CrossOrigin(origins = { "http://localhost:3000" })
//@CrossOrigin(origins = { "http://i4a306.p.ssafy.io" })
@RestController
@RequestMapping("/likes")
public class LikesController {

	public static final Logger logger = LoggerFactory.getLogger(LikesController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	@Autowired
	LikesService likesservice;

	////////// 좋아요 추가 ///////////
	@PostMapping("/addlikes")
	public ResponseEntity<Map<String, Object>> addlikes(@RequestBody LikesDto likesdto) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		int user_id = likesdto.getUserid();
		int post_id = likesdto.getPostid();

		try {
			likesservice.addLike(user_id, post_id);
			resultMap.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);

	}

	////////// 좋아요 취소 ///////////
	@DeleteMapping("/unlikes/{uid}/{pid}")
	public ResponseEntity<Map<String, Object>> unlikes(@PathVariable("uid") int userid,
			@PathVariable("pid") int postid, HttpServletRequest request) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		try {
			likesservice.unLike(userid, postid);
			resultMap.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	////////// 좋아요 수 카운트 ///////////
	@GetMapping("/countlikes/{pid}")
	public ResponseEntity<Map<String, Object>> likescount(@PathVariable("pid") int pid, HttpServletRequest request)
			throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;
		logger.info("좋아요 수 카운트 시작");
		try {
			int count = likesservice.countLikes(pid);
			resultMap.put("count", count);
			resultMap.put("message", SUCCESS);
			status = HttpStatus.ACCEPTED;
		} catch (Exception e) {
			logger.error("좋아요 수 카운트 실패");
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

	////////// 좋아요 상태 체크 ///////////
	@GetMapping("/checklikes/{uid}/{pid}")
	public ResponseEntity<Map<String, Object>> likescheck(@PathVariable("uid") int userid,
			@PathVariable("pid") int postid, HttpServletRequest request) throws Exception {
		Map<String, Object> resultMap = new HashMap<>();
		HttpStatus status = null;

		try {
			if (likesservice.checkLikes(userid, postid)) {
				resultMap.put("message", SUCCESS);
				status = HttpStatus.ACCEPTED;
			} else {
				resultMap.put("message", FAIL);
				status = HttpStatus.ACCEPTED;
			}
		} catch (Exception e) {
			resultMap.put("message", e.getMessage());
			status = HttpStatus.INTERNAL_SERVER_ERROR;
		}

		return new ResponseEntity<Map<String, Object>>(resultMap, status);
	}

}
