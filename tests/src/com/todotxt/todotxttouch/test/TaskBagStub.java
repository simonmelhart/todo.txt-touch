package com.todotxt.todotxttouch.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.todotxt.todotxttouch.task.Filter;
import com.todotxt.todotxttouch.task.Priority;
import com.todotxt.todotxttouch.task.Task;
import com.todotxt.todotxttouch.task.TaskBag;

public class TaskBagStub implements TaskBag {

	@Override
	public void archive() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAsTask(String input) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Task> getTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Task> getTasks(Filter<Task> filter, Comparator<Task> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<String> getProjects() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getContexts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Priority> getPriorities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void syncWithRemote(boolean overwrite) {
		// TODO Auto-generated method stub
		syncWithRemote(false, overwrite);
	}

	@Override
	public void syncWithRemote(boolean overridePreference, boolean overwrite) {
		// TODO Auto-generated method stub
		++syncWithRemoteCalled;

	}
	public int syncWithRemoteCalled = 0;

}
