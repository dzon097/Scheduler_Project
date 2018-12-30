package com.etf.os2.project.scheduler;

import java.util.PriorityQueue;

import com.etf.os2.project.process.Pcb;
import com.etf.os2.project.process.PcbData;
import com.etf.os2.project.process.Pcb.ProcessState;

public class SJFScheduler extends Scheduler {
	private static long TAUSTART = 2;

	private float alfa;
	private boolean preempty;
	private PriorityQueue<PcbPrioritySJFS> queue;

	public SJFScheduler(String[] args) /**
										 * prvi argument stepen usredljavanja, drugi tip algoritma 0 nonpreemty !=0
										 * preemty
										 */
	{
		alfa = Float.parseFloat(args[0]);
		int n = Integer.parseInt(args[1]);
		if (n != 0)
			preempty = true;
		else
			preempty = false;
		queue = new PriorityQueue<>();
	}

	@Override
	public Pcb get(int cpuId) {
		if (queue.size() != 0) {
			PcbPrioritySJFS pp = queue.remove();
			Pcb pcb = pp.getPcb();
			pcb.setTimeslice(0);
			if (preempty)
				Pcb.RUNNING[cpuId].setPreempt(!preempty);

			return pcb;
		}
		return null;
	}

	@Override
	public void put(Pcb pcb) {
		if (pcb == null)
			return;
		PcbData pd = pcb.getPcbData();
		if (pd == null)
			pcb.setPcbData(new PcbData());

		if (ProcessState.CREATED == pcb.getPreviousState()) {
			pcb.getPcbData().setTau(TAUSTART);
		} else {
			if (ProcessState.BLOCKED == pcb.getPreviousState()) {
				pcb.getPcbData().setTau((long) ((pcb.getExecutionTime() + pcb.getPcbData().getTau()) * alfa));

			} else if (ProcessState.RUNNING == pcb.getPreviousState()) {
				int k = 0;
				for (k = 0; k < Pcb.RUNNING.length; k++)
					if (pcb.getId() == Pcb.RUNNING[k].getId())
						break;
				pcb.getPcbData().setCPU(k);
			}
			if (preempty)
				Pcb.RUNNING[pcb.getPcbData().getCPU()].setPreempt(preempty);
		}

		boolean b = queue.add(new PcbPrioritySJFS(pcb));
		if (!b)
			System.out.println("***** Nije ubacen u red **** ");

	}

}
