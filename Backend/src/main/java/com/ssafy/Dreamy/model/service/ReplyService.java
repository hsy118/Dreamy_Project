package com.ssafy.Dreamy.model.service;

import java.util.List;

import com.ssafy.Dreamy.model.ReplyDto;

public interface ReplyService {

	public int insert(ReplyDto replyDto) throws Exception;
	public int countList(int pid) throws Exception;
	public List<ReplyDto> getList(int pid) throws Exception;
	public int delete(int rid) throws Exception;
	
}
