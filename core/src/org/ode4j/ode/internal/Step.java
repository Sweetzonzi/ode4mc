/*************************************************************************
 *                                                                       *
 * Open Dynamics Engine, Copyright (C) 2001,2002 Russell L. Smith.       *
 * All rights reserved.  Email: russ@q12.org   Web: www.q12.org          *
 * Open Dynamics Engine 4J, Copyright (C) 2007-2010 Tilmann Zäschke      *
 * All rights reserved.  Email: ode4j@gmx.de   Web: www.ode4j.org        *
 *                                                                       *
 * This library is free software; you can redistribute it and/or         *
 * modify it under the terms of EITHER:                                  *
 *   (1) The GNU Lesser General Public License as published by the Free  *
 *       Software Foundation; either version 2.1 of the License, or (at  *
 *       your option) any later version. The text of the GNU Lesser      *
 *       General Public License is included with this library in the     *
 *       file LICENSE.TXT.                                               *
 *   (2) The BSD-style license that is included with this library in     *
 *       the file ODE-LICENSE-BSD.TXT and ODE4J-LICENSE-BSD.TXT.         *
 *                                                                       *
 * This library is distributed in the hope that it will be useful,       *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the files    *
 * LICENSE.TXT, ODE-LICENSE-BSD.TXT and ODE4J-LICENSE-BSD.TXT for more   *
 * details.                                                              *
 *                                                                       *
 *************************************************************************/
package org.ode4j.ode.internal;

import static org.cpp4j.Cstdio.stdout;
import static org.ode4j.ode.OdeConstants.dInfinity;
import static org.ode4j.ode.OdeMath.dMultiply0_133;
import static org.ode4j.ode.OdeMath.dMultiply0_331;
import static org.ode4j.ode.OdeMath.dMultiply0_333;
import static org.ode4j.ode.OdeMath.dMultiply2_333;
import static org.ode4j.ode.OdeMath.dMultiplyAdd0_331;
import static org.ode4j.ode.OdeMath.dSubtractVectorCross3;
import static org.ode4j.ode.internal.Common.dIASSERT;
import static org.ode4j.ode.internal.Common.dPAD;
import static org.ode4j.ode.internal.Common.dRecip;
import static org.ode4j.ode.internal.Matrix.dSetValue;
import static org.ode4j.ode.internal.Timer.dTimerEnd;
import static org.ode4j.ode.internal.Timer.dTimerNow;
import static org.ode4j.ode.internal.Timer.dTimerReport;
import static org.ode4j.ode.internal.Timer.dTimerStart;

import org.cpp4j.FILE;
import org.ode4j.math.DMatrix3;
import org.ode4j.ode.DJoint.DJointFeedback;
import org.ode4j.ode.internal.joints.DxJoint;
import org.ode4j.ode.internal.joints.DxJointNode;
import org.ode4j.ode.internal.processmem.DxUtil.BlockPointer;
import org.ode4j.ode.internal.processmem.DxWorldProcessIslandsInfo.dmemestimate_fn_t;
import org.ode4j.ode.internal.processmem.DxWorldProcessMemArena;


class Step extends AbstractStepper implements DxWorld.dstepper_fn_t,
dmemestimate_fn_t {

	public static final Step INSTANCE = new Step();

	//****************************************************************************
	// misc defines

	//#define FAST_FACTOR
	private static final boolean FAST_FACTOR = true;
	//#define TIMING  /was commented out!
	private static final boolean TIMING = false;
	private static final void IFTIMING_dTimerStart(String name) {
	    if (TIMING) {
	        dTimerStart(name);
	    }
	}
	private static final void IFTIMING_dTimerNow(String name) {
	    if (TIMING) {
	        dTimerNow(name);
	    }
	}
	private static final void IFTIMING_dTimerEnd() {
        if (TIMING) {
            dTimerEnd();
        }
	}
	private static final void IFTIMING_dTimerReport (FILE fout, int average) {
	    if (TIMING) {
	        dTimerReport(fout, average);
	    }
	}

	public static final boolean DIRECT_CHOLESKY = true;
	public static final boolean REPORT_ERROR = false;

	//****************************************************************************
	// special matrix multipliers

	// this assumes the 4th and 8th rows of B and C are zero.

	private static void Multiply2_p8r (double[] A, int APos,
			double[] B, int BPos, double[] C, int CPos,
			int p, int r, int Askip)
	{
		dIASSERT (p>0 && r>0);
//		dAASSERT(A, B, C);
		final int Askip_munus_r = Askip - r;
		int aa = APos;//dReal *aa = A;
		int bb = BPos;//const dReal *bb = B;
		for (int i = p; i != 0; --i) {
		    int cc = CPos;//const dReal *cc = C;
		    for (int j = r; j != 0; --j) {
		        double sum;
				sum  = B[bb] * C[cc]; //bb[0]*cc[0];
				sum += B[bb+1] * C[cc+1]; //sum += bb[1]*cc[1];
				sum += B[bb+2] * C[cc+2]; //sum += bb[2]*cc[2];
				sum += B[bb+4] * C[cc+4]; //sum += bb[4]*cc[4];
				sum += B[bb+5] * C[cc+5]; //sum += bb[5]*cc[5];
				sum += B[bb+6] * C[cc+6]; //sum += bb[6]*cc[6];
				A[aa++] = sum;//*(A++) = sum; 
				cc +=8;//cc += 8;
			}
		    bb += 8;
		    aa += Askip_munus_r;
		}
	}


	// this assumes the 4th and 8th rows of B and C are zero.

	private static void MultiplyAdd2_p8r (double[] A, int APos,
			double[] B, int BPos, double[] C, int CPos,
			int p, int r, int Askip)
	{
		dIASSERT (p>0 && r>0);
//		dAASSERT(A, B, C);
		final int Askip_munus_r = Askip - r;
		dIASSERT(Askip >= r);
		int aa = APos;//  dReal *aa = A;
		int bb = BPos;//const dReal *bb = B;
		for (int i = p; i != 0; --i) {
		    int cc = CPos;//const dReal *cc = C;
		    for (int j = r; j != 0; --j) {
		        double sum;
				sum  = B[bb] * C[cc]; //sum = bb[0]*cc[0];
				sum += B[bb+1] * C[cc+1]; //sum += bb[1]*cc[1];
				sum += B[bb+2] * C[cc+2]; //sum += bb[2]*cc[2];
				sum += B[bb+4] * C[cc+4]; //sum += bb[4]*cc[4];
				sum += B[bb+5] * C[cc+5]; //sum += bb[5]*cc[5];
				sum += B[bb+6] * C[cc+6]; //sum += bb[6]*cc[6];
				A[aa++] += sum;//*(aa++) += sum; 
				cc += 8;
		    }
		    bb += 8;
		    aa += Askip_munus_r;
		}
	}


	// this assumes the 4th and 8th rows of B are zero.

	private static void MultiplySub0_p81 (double[] A, final int APos,
			double[] B, final int BPos, double[] C, final int CPos, int p)
	{
		dIASSERT (p>0);
//		dAASSERT(A, B, C);
		int aa = APos;//dReal *aa = A;
		int bb = BPos;//const dReal *bb = B;
		for (int i = p; i != 0; --i) {
		    double sum;
			sum  = B[0+bb]*C[0+CPos];
			sum += B[1+bb]*C[1+CPos];
			sum += B[2+bb]*C[2+CPos];
			sum += B[4+bb]*C[4+CPos];
			sum += B[5+bb]*C[5+CPos];
			sum += B[6+bb]*C[6+CPos];
			A[aa++] -= sum;//*(aa++) = sum;
			bb += 8;//B += 8;
		}
	}


	// this assumes the 4th and 8th rows of B are zero.

	private static void MultiplyAdd1_8q1 (double[] A, int APos, 
			double[] B, int BPos, double[] C, int CPos, int q)
	{
	    dIASSERT (q>0);
//	    dAASSERT(A, B, C);
	    int bb = BPos;//const dReal *bb = B;
	    double sum0 = 0, sum1 = 0, sum2 = 0, sum4=0, sum5 = 0, sum6 = 0;
	    for (int k = 0; k < q; ++k) {
	        final double C_k = C[k+CPos];
	        sum0 += B[bb+0] * C_k;
	        sum1 += B[bb+1] * C_k;
	        sum2 += B[bb+2] * C_k;
	        sum4 += B[bb+4] * C_k;
	        sum5 += B[bb+5] * C_k;
	        sum6 += B[bb+6] * C_k;
	        bb += 8;
	    }
	    A[0+APos] += sum0;
	    A[1+APos] += sum1;
	    A[2+APos] += sum2;
	    A[4+APos] += sum4;
	    A[5+APos] += sum5;
	    A[6+APos] += sum6;
	}


	// this assumes the 4th and 8th rows of B are zero.

	private static void Multiply1_8q1 (double[] A, final int APos, 
			double[] B, final int BPos, double[] C, final int CPos, int q)
	{
	    int bb = BPos;//const dReal *bb = B;
	    double sum0 = 0, sum1 = 0, sum2 = 0, sum4=0, sum5 = 0, sum6 = 0;
	    for (int k = 0; k < q; ++k) {
	      final double C_k = C[k+CPos];
	      sum0 += B[bb+0] * C_k;
	      sum1 += B[bb+1] * C_k;
	      sum2 += B[bb+2] * C_k;
	      sum4 += B[bb+4] * C_k;
	      sum5 += B[bb+5] * C_k;
	      sum6 += B[bb+6] * C_k;
	      bb += 8;
	    }
	    A[0+APos] = sum0;
	    A[1+APos] = sum1;
	    A[2+APos] = sum2;
	    A[4+APos] = sum4;
	    A[5+APos] = sum5;
	    A[6+APos] = sum6;
	}

	//****************************************************************************
	// an optimized version of dInternalStepIsland1()

	private static class dJointWithInfo1
	{
	  DxJoint joint;
	  DxJoint.Info1 info = new DxJoint.Info1();
	};

	//void dInternalStepIsland_x1 (dxWorld *world, dxBody * const *body, int nb,
	//	     dxJoint * const *_joint, int nj, double stepsize)
	private void dInternalStepIsland_x2 (DxWorldProcessMemArena memarena,
	        DxWorld world, final DxBody [] bodyA, final int bOfs, 
			int nb,
			final DxJoint [] _jointA, final int jOfs, int _nj, double stepsize)
	{
	    final double stepsizeRecip = dRecip(stepsize);

	    {
	      // number all bodies in the body list - set their tag values
	      for (int i=0; i<nb; ++i) bodyA[bOfs+i].tag = i;
	    }

		// for all bodies, compute the inertia tensor and its inverse in the global
		// frame, and compute the rotational force and add it to the torque
		// accumulator. invI are vertically stacked 3x4 matrices, one per body.
		// @@@ check computation of rotational force.
	    memarena.dummy();
	    double[] invI = new double[3*4*nb];//memarena.AllocateArray<dReal> (3*4*(size_t)nb);

	    { // Identical to QuickStep
	        int invIrow = 0;////	      dReal *invIrow = invI;
//	      dxBody *const *const bodyend = body + nb;
//	      for (dxBody *const *bodycurr = body; bodycurr != bodyend; invIrow += 12, ++bodycurr) {
            DMatrix3 tmp = new DMatrix3();
            DMatrix3 I = new DMatrix3();
	        for (int ii = 0; ii < nb; ii++, invIrow+=12) {
	            DxBody b = bodyA[bOfs+ii];

	            // compute inverse inertia tensor in global frame
	            dMultiply2_333 (tmp,b.invI,b._posr.R);
	            dMultiply0_333 (invI,invIrow,b._posr.R,tmp);

	            if (b.isFlagsGyroscopic()) {//flags & DxBody.dxBodyGyroscopic) {
	                // compute inertia tensor in global frame
	                dMultiply2_333 (tmp,b.mass._I,b._posr.R);
	                dMultiply0_333 (I,b._posr.R,tmp);
	                // compute rotational force
	                dMultiply0_331 (tmp,I,b.avel);
	                dSubtractVectorCross3 (b.tacc,b.avel,tmp);
	            }
	        }
	    }

	    { // Identical to QuickStep
	        // add the gravity force to all bodies
	        // since gravity does normally have only one component it's more efficient
	        // to run three loops for each individual component
	        double gravity_x = world.gravity.get0();
	        if (gravity_x!=0) {
	            for (int ii = bOfs; ii < bOfs+nb; ii++) {
	                DxBody b = bodyA[ii];
	                if (b.getGravityMode()) {
	                    b.facc.add(0, b.mass._mass * gravity_x);
	                }
	            }
	        }
	        double gravity_y = world.gravity.get1();
	        if (gravity_y!=0) {
	            for (int ii = bOfs; ii < bOfs+nb; ii++) {
	                DxBody b = bodyA[ii];
	                if (b.getGravityMode()) {
	                    b.facc.add(1, b.mass._mass * gravity_y);
	                }
	            }
	        }
	        double gravity_z = world.gravity.get2();
	        if (gravity_z!=0) {
	            for (int ii = bOfs; ii < bOfs+nb; ii++) {
	                DxBody b = bodyA[ii];
	                if (b.getGravityMode()) {
	                    b.facc.add(2, b.mass._mass * gravity_z);
	                }
	            }
	        }
	    }

		// get m = total constraint dimension, nub = number of unbounded variables.
		// create constraint offset array and number-of-rows array for all joints.
		// the constraints are re-ordered as follows: the purely unbounded
		// constraints, the mixed unbounded + LCP constraints, and last the purely
		// LCP constraints. this assists the LCP solver to put all unbounded
	    // variables at the start for a quick factorization.
		//
		// joints with m=0 are inactive and are removed from the joints array
		// entirely, so that the code that follows does not consider them.
	    // also number all active joints in the joint list (set their tag values).
	    // inactive joints receive a tag value of -1.

	    // Reserve twice as much memory and start from the middle so that regardless of 
	    // what direction the array grows to there would be sufficient room available.
	    final int ji_reserve_count = 2 * _nj;
	    //dJointWithInfo1 *jointiinfos = memarena->AllocateArray<dJointWithInfo1> (ji_reserve_count);
	    memarena.dummy();
	    dJointWithInfo1[] jointiinfosA = new dJointWithInfo1[ji_reserve_count];
	    for (int i = 0; i < jointiinfosA.length; i++) {
	        //TODO TZ: optimize, for example use two separate arrays for the two attributes
	        jointiinfosA[i] = new dJointWithInfo1();
	    }
	    int jiP = 0;
	    int nub;
	    int ji_start, ji_end;

	    {
	        int unb_start, mix_start, mix_end, lcp_end;
	        unb_start = mix_start = mix_end = lcp_end = _nj;

//	      dJointWithInfo1 *jicurr = jointiinfos + lcp_end;
//	      dxJoint *const *const _jend = _joint + _nj;
//	      dxJoint *const *_jcurr = _joint;
	        dJointWithInfo1 jicurrO = jointiinfosA.length > 0 ? jointiinfosA[lcp_end+jiP] : null;
	        int jicurrP = lcp_end;
	        final int _jend = _nj; 
	        int _jcurrP = 0;
	        //DxJoint _jcurrO = null;
	        while (true) {
	            // -------------------------------------------------------------------------
	            // Switch to growing array forward
	            {
	                boolean fwd_end_reached = false;
	                dJointWithInfo1 jimixendO = jointiinfosA.length > 0 ? jointiinfosA[mix_end+jiP] : null;
	                int jimixendP = mix_end;
	                while (true) {  // jicurr=dest, _jcurr=src
	                    if (_jcurrP == _jend) {
	                        lcp_end = jicurrP;// - jointiinfos;
	                        fwd_end_reached = true;
	                        break;
	                    }
	                    DxJoint j = _jointA[jOfs+_jcurrP++];//*_jcurr++;
	                    //_jcurrO = _jointA[jOfs+_jcurrP];//TZ TODO?? Fails in Plane2DDemo
	                    j.getInfo1 (jicurrO.info);
	                    dIASSERT (jicurrO.info.m >= 0 && jicurrO.info.m <= 6 && jicurrO.info.nub >= 0 && jicurrO.info.nub <= jicurrO.info.m);
	                    if (jicurrO.info.m > 0) {
	                        if (jicurrO.info.nub == 0) { // A lcp info - a correct guess!!!
	                            jicurrO.joint = j;
	                            ++jicurrP;
	                            jicurrO = (jicurrP+jiP) < jointiinfosA.length ? jointiinfosA[jicurrP+jiP] : null;//TZ
	                        } else if (jicurrO.info.nub < jicurrO.info.m) { // A mixed case
	                            if (unb_start == mix_start) { // no unbounded infos yet - just move to opposite side of mixed-s
	                                unb_start = mix_start = mix_start - 1;
	                                dJointWithInfo1 jimixstart = jointiinfosA[mix_start+jiP];
	                                jimixstart.info = jicurrO.info;
	                                jimixstart.joint = j;
	                            } else if (jimixendP != jicurrP) { // have to swap to the tail of mixed-s
	                                DxJoint.Info1 tmp_info = jicurrO.info;
	                                jicurrP = jimixendP;
	                                jicurrO = jointiinfosA[jicurrP+jiP];//TZ
	                                jimixendO.info = tmp_info;
	                                jimixendO.joint = j;
	                                ++jimixendP; ++jicurrP;
	                                jicurrO = jointiinfosA[jicurrP+jiP];//TZ
	                                jimixendO = jointiinfosA[jimixendP+jiP];//TZ
	                            } else { // no need to swap as there are no LCP info-s yet
	                                jicurrO.joint = j;
	                                jimixendP = jicurrP = jicurrP + 1;
	                                jicurrO = jointiinfosA[jicurrP+jiP];//TZ
	                                jimixendO = jointiinfosA[jimixendP+jiP];//TZ
	                            }
	                        } else { // A purely unbounded case -- break out and proceed growing in opposite direction
	                            unb_start = unb_start - 1;
	                            dJointWithInfo1 jiunbstartO = jointiinfosA[unb_start+jiP];
	                            int jiunbstartP = unb_start;
	                            jiunbstartO.info = jicurrO.info;
	                            jiunbstartO.joint = j;
	                            lcp_end = jicurrP;// - jointiinfos;
	                            mix_end = jimixendP;// - jointiinfos;
	                            jicurrP = jiunbstartP - 1;
	                            if (jicurrP >= 0) {
	                                jicurrO = jointiinfosA[jicurrP+jiP];//TZ
	                            } else {
	                                //TODO TZ is this good??
	                                jicurrO = null;
	                            }
	                            break;
	                        }
	                    } else {
	                        j.tag = -1;
	                    }
	                }
	                if (fwd_end_reached) {
	                    break;
	                }
	            }
	            // -------------------------------------------------------------------------
	            // Switch to growing array backward
	            {
	                boolean bkw_end_reached = false;
	                dJointWithInfo1 jimixstartO = jointiinfosA[mix_start - 1 +jiP];
	                int jimixstartP = mix_start - 1;
	                while (true) {  // jicurr=dest, _jcurr=src
	                    if (_jcurrP == _jend) {
	                        unb_start = (jicurrP + 1);// - jointiinfos;
	                        mix_start = (jimixstartP + 1);// - jointiinfos;
	                        bkw_end_reached = true;
	                        break;
	                    }
	                    DxJoint j = _jointA[jOfs+_jcurrP++];//*_jcurr++;
	                   // _jcurrO = _jointA[jOfs+_jcurrP]; //TZ TODO?
	                    j.getInfo1 (jicurrO.info); //TZ
	                    dIASSERT (jicurrO.info.m >= 0 && jicurrO.info.m <= 6 && jicurrO.info.nub >= 0 && jicurrO.info.nub <= jicurrO.info.m);
	                    if (jicurrO.info.m > 0) {
	                        if (jicurrO.info.nub == jicurrO.info.m) { // An unbounded info - a correct guess!!!
	                            jicurrO.joint = j;
	                            --jicurrP;
	                            //_jcurrO = _jointA[jOfs+_jcurrP]; //TZ TODO?
	                        } else if (jicurrO.info.nub > 0) { // A mixed case
	                            if (mix_end == lcp_end) { // no lcp infos yet - just move to opposite side of mixed-s
	                                dJointWithInfo1 jimixend = jointiinfosA[mix_end+jiP];
	                                lcp_end = mix_end = mix_end + 1;
	                                jimixend.info = jicurrO.info;
	                                jimixend.joint = j;
	                            } else if (jimixstartP != jicurrP) { // have to swap to the head of mixed-s
	                                DxJoint.Info1 tmp_info = jicurrO.info;
	                                //*jicurr = *jimixstart;
	                                jicurrP = jimixstartP;
	                                jicurrO = jointiinfosA[jicurrP+jiP]; //TZ
	                                jimixstartO.info = tmp_info;
	                                jimixstartO.joint = j;
	                                --jimixstartP; --jicurrP;
	                                jicurrO = jointiinfosA[jicurrP+jiP]; //TZ
	                                jimixstartO = jointiinfosA[jimixstartP+jiP]; //TZ
	                            } else { // no need to swap as there are no unbounded info-s yet
	                                jicurrO.joint = j;
	                                jimixstartP = jicurrP = jicurrP - 1;
	                                jicurrO = jointiinfosA[jicurrP+jiP]; //TZ
	                                jimixstartO = jointiinfosA[jimixstartP+jiP]; //TZ
	                            }
	                        } else { // A purely lcp case -- break out and proceed growing in opposite direction
	                            dJointWithInfo1 jilcpendO = jointiinfosA[lcp_end+jiP];
	                            int jilcpendP = lcp_end;
	                            lcp_end = lcp_end + 1;
	                            jilcpendO.info = jicurrO.info;
	                            jilcpendO.joint = j;
	                            unb_start = (jicurrP + 1);// - jointiinfos;
	                            mix_start = (jimixstartP + 1);// - jointiinfos;
	                            jicurrP = jilcpendP + 1;
	                            jicurrO = jointiinfosA[jicurrP+jiP]; //TZ
	                            break;
	                        }
	                    } else {
	                        j.tag = -1;
	                    }
	                }
	                if (bkw_end_reached) {
	                    break;
	                }
	            }
	        }

	        nub = mix_start - unb_start;
	        //dIASSERT((size_t)(mix_start - unb_start) <= (size_t)UINT_MAX);
	        ji_start = unb_start;
	        ji_end = lcp_end;
	    }

	    //memarena.ShrinkArray<dJointWithInfo1>(jointiinfos, ji_reserve_count, ji_end);
	    memarena.dummy();
	    jiP = ji_start; //jointiinfos += ji_start;
	    int nj = (int)(ji_end - ji_start);
	    //dIASSERT((size_t)(ji_end - ji_start) <= (size_t)UINT_MAX);

	    int m = 0;

	    {
	        int mcurr = 0;
	        dJointWithInfo1 jicurrO = null;//jointiinfosA[jiP];
            int jicurrP = 0;
	        int jiend = jicurrP + nj;
	        for (int i = 0; jicurrP != jiend; i++, ++jicurrP) {
	            jicurrO = jointiinfosA[jiP+jicurrP];
	            jicurrO.joint.tag = i;
	            int jm = jicurrO.info.m;
	            mcurr += jm;
	        }

	        m = mcurr;
	    }

	    // this will be set to the force due to the constraints
	    //dReal *cforce = memarena->AllocateArray<dReal> ((size_t)nb*8);
        memarena.dummy();
        double[] cforce = new double[nb*8];
        //dSetZero (cforce,(size_t)nb*8);
	    
        // if there are constraints, compute cforce
        if (m > 0) {
          // create a constraint equation right hand side vector `c', a constraint
          // force mixing vector `cfm', and LCP low and high bound vectors, and an
          // 'findex' vector.
            double[] lo, hi, J, A, rhs;
            int[] findex;

            {
                final int mlocal = m;

                memarena.dummy(); //multiple!!

                //lo = memarena->AllocateArray<dReal> (mlocal);
                lo = new double[mlocal];
                dSetValue (lo,mlocal,-dInfinity);

                //hi = memarena->AllocateArray<dReal> (mlocal);
                hi = new double[mlocal];
                dSetValue (hi,mlocal, dInfinity);

                //J = memarena->AllocateArray<dReal> (2*8*(size_t)mlocal);
                J = new double[2*8*mlocal];
                //TZ unnecessary:
                //dSetZero (J,2*8*(size_t)mlocal);

                //findex = memarena->AllocateArray<int> (mlocal);
                findex = new int[mlocal];
                for (int i=0; i<mlocal; ++i) findex[i] = -1;

                int mskip = dPAD(mlocal);
                //A = memarena->AllocateArray<dReal> (mlocal*(size_t)mskip);
                A = new double[mlocal*mskip];
                //dSetZero (A,mlocal*(size_t)mskip);

                //rhs = memarena->AllocateArray<dReal> (mlocal);
                rhs = new double[mlocal];
                //dSetZero (rhs,mlocal);
            }

            // Put 'c' in the same memory as 'rhs' as they transit into each other
            double[] c = rhs; rhs = null; // erase rhs pointer for now as it is not to be used yet

            //BEGIN_STATE_SAVE(memarena, cfmstate)
            BlockPointer cfmstate = memarena.BEGIN_STATE_SAVE();
            {
                //dReal *cfm = memarena->AllocateArray<dReal> (m);
                memarena.dummy();
                double[] cfm = new double[m];
                dSetValue (cfm,m,world.global_cfm);

                //dReal *JinvM = memarena->AllocateArray<dReal> (2*8*(size_t)m);
                memarena.dummy();
                double[] JinvM = new double[2*8*m];
                //dSetZero (JinvM,2*8*(size_t)m);

                {
                    IFTIMING_dTimerNow ("create J");
                    // get jacobian data from constraints. a (2*m)x8 matrix will be created
                    // to store the two jacobian blocks from each constraint. it has this
                    // format:
                    //
                    //   l l l 0 a a a 0  \    .
                    //   l l l 0 a a a 0   }-- jacobian body 1 block for joint 0 (3 rows)
                    //   l l l 0 a a a 0  /
                    //   l l l 0 a a a 0  \    .
                    //   l l l 0 a a a 0   }-- jacobian body 2 block for joint 0 (3 rows)
                    //   l l l 0 a a a 0  /
                    //   l l l 0 a a a 0  }--- jacobian body 1 block for joint 1 (1 row)
                    //   l l l 0 a a a 0  }--- jacobian body 2 block for joint 1 (1 row)
                    //   etc...
                    //
                    //   (lll) = linear jacobian data
                    //   (aaa) = angular jacobian data
                    //

                    DxJoint.Info2 Jinfo = new DxJoint.Info2();
                    Jinfo.setRowskip(8);
                    Jinfo.setArrays(J, c, cfm, lo, hi, findex); //TZ
                    Jinfo.fps = stepsizeRecip;
                    Jinfo.erp = world.getERP();

                    int ofsi = 0;
                    //            dJointWithInfo1 jicurr = jointiinfos;
                    //            dJointWithInfo1 jiend = jicurr + nj;
                    //            for (; jicurr != jiend; ++jicurr) {
                    for (int ii = 0; ii < nj; ii++) {
                        final dJointWithInfo1 jicurr = jointiinfosA[jiP+ii];  //TZ this include jiP!
                        final int infom = jicurr.info.m;
                        final int J1rowP = 0 + 2*8*ofsi;//*const J1row = J + 2*8*(size_t)ofsi;
                        Jinfo.J1lp = J1rowP;
                        Jinfo.J1ap = J1rowP + 4;
                        final int J2rowP = J1rowP + 8*infom;//dReal *const J2row = J1row + 8*(size_t)infom;
                        Jinfo.J2lp = J2rowP;
                        Jinfo.J2ap = J2rowP + 4;
                        //              Jinfo.c = c + ofsi;
                        //              Jinfo.cfm = cfm + ofsi;
                        //              Jinfo.lo = lo + ofsi;
                        //              Jinfo.hi = hi + ofsi;
                        //              Jinfo.findex = findex + ofsi;
                        Jinfo.setAllP(ofsi);

                        DxJoint joint = jicurr.joint;
                        joint.getInfo2 (Jinfo);

                        // adjust returned findex values for global index numbering
                        int findex_ofsiP = ofsi;//findex + ofsi;
                        for (int j=0; j<infom; ++j) {
                            int fival = findex[findex_ofsiP+j];//findex_ofsi[j];
                            if (fival != -1) 
                                //findex_ofsi[j] = fival + ofsi;
                                findex[findex_ofsiP+j] = fival + ofsi;
                        }

                        ofsi += infom;
                    }
                }

                {
                    IFTIMING_dTimerNow ("compute A");
                    {
                        // compute A = J*invM*J'. first compute JinvM = J*invM. this has the same
                        // format as J so we just go through the constraints in J multiplying by
                        // the appropriate scalars and matrices.
                        int ofsi = 0;
                        dJointWithInfo1 jicurrO = jointiinfosA[jiP];
                        int jicurrP = 0;
                        int jiendP = jicurrP + nj;
                        for (; jicurrP != jiendP; ++jicurrP) {
                            jicurrO = jointiinfosA[jiP+jicurrP];//TZ
                            final int infom = jicurrO.info.m;
                            DxJoint joint = jicurrO.joint;
                            int b0 = joint.node[0].body.tag;
                            double body_invMass0 = bodyA[bOfs+b0].invMass;
                            int body_invI0 = b0*12;//invI + b0*12;
                            int Jsrc = 2*8*ofsi;//J + 2*8*ofsi;
                            int Jdst = 2*8*ofsi;//JinvM + 2*8*ofsi;
                            for (int j=infom; j>0;) {
                                j -= 1;
                                //for (int k=0; k<3; ++k) Jdst[k] = Jsrc[k] * body_invMass0;
                                for (int k=0; k<3; ++k) JinvM[Jdst+k] = J[Jsrc+k] * body_invMass0;
                                dMultiply0_133 (JinvM,Jdst+4,J,Jsrc+4,invI,body_invI0);
                                Jsrc += 8;
                                Jdst += 8;
                            }

                            if (joint.node[1].body!=null) {
                                int b1 = joint.node[1].body.tag;
                                double body_invMass1 = bodyA[bOfs+b1].invMass;
                                int body_invI1 = b1*12;//invI + b1*12;
                                for (int j=infom; j>0; ) {
                                    j -= 1;
                                    //for (int k=0; k<3; ++k) Jdst[k] = Jsrc[k] * body_invMass1;
                                    for (int k=0; k<3; ++k) JinvM[Jdst+k] = J[Jsrc+k] * body_invMass1;
                                    dMultiply0_133 (JinvM,Jdst+4,J,Jsrc+4,invI,body_invI1);
                                    Jsrc += 8;
                                    Jdst += 8;
                                }
                            }

                            ofsi += infom;
                        }
                    }

                    {
                        // now compute A = JinvM * J'. A's rows and columns are grouped by joint,
                        // i.e. in the same way as the rows of J. block (i,j) of A is only nonzero
                        // if joints i and j have at least one body in common. 

                        //BEGIN_STATE_SAVE(memarena, ofsstate)
                        BlockPointer ofsstate = memarena.BEGIN_STATE_SAVE();
                        {
                            //unsigned int *ofs = memarena->AllocateArray<unsigned int> (m);
                            memarena.dummy();
                            int[] ofs = new int[m];
                            final int mskip = dPAD(m);

                            int ofsi = 0;
                            dJointWithInfo1 jicurrO = jointiinfosA[jiP];
                            int jicurrP = 0;
                            int jiendP = jicurrP + nj;
                            for (int i = 0; jicurrP != jiendP; i++, ++jicurrP) {
                                jicurrO = jointiinfosA[jiP+jicurrP]; //TZ
                                int infom = jicurrO.info.m;
                                DxJoint joint = jicurrO.joint;

                                int Arow = mskip*ofsi;// +A;
                                int JinvMrow = 2*8*ofsi; // JinvM + 

                                DxBody jb0 = joint.node[0].body;
                                for (DxJointNode n0=jb0.firstjoint.get(); n0!=null; n0=n0.next) {
                                    // if joint was tagged as -1 then it is an inactive (m=0 or disabled)
                                    // joint that should not be considered
                                    int j0 = n0.joint.tag;
                                    if (j0 != -1 && j0 < i) {
                                        final dJointWithInfo1 jiother = jointiinfosA[jiP + j0];
                                        int ofsother = (jiother.joint.node[1].body == jb0) ? 8*jiother.info.m : 0;
                                        // set block of A
                                        MultiplyAdd2_p8r (A,Arow + ofs[j0], JinvM, JinvMrow, 
                                                J, 2*8*ofs[j0] + ofsother, infom, jiother.info.m, mskip);
                                    }
                                }

                                DxBody jb1 = joint.node[1].body;
                                dIASSERT(jb1 != jb0);
                                if (jb1!=null)
                                {
                                    for (DxJointNode n1=jb1.firstjoint.get(); n1!=null; n1=n1.next) {
                                        // if joint was tagged as -1 then it is an inactive (m=0 or disabled)
                                        // joint that should not be considered
                                        int j1 = n1.joint.tag;
                                        if (j1 != -1 && j1 < i) {
                                            final dJointWithInfo1 jiother = jointiinfosA[jiP + j1];
                                            int ofsother = (jiother.joint.node[1].body == jb1) ? 8*jiother.info.m : 0;
                                            // set block of A
                                            MultiplyAdd2_p8r (A,Arow + ofs[j1], JinvM, JinvMrow + 8*infom, 
                                                    J, 2*8*ofs[j1] + ofsother, infom, jiother.info.m, mskip);
                                        }
                                    }
                                }

                                ofs[i] = ofsi;
                                ofsi += infom;
                            }

                        } //END_STATE_SAVE(memarena, ofsstate);
                        memarena.END_STATE_SAVE(ofsstate);
                    }

                    {
                        // compute diagonal blocks of A
                        final int mskip = dPAD(m);

                        int ofsi = 0;
                        dJointWithInfo1 jicurrO = jointiinfosA[jiP];
                        int jicurrP = 0;
                        int jiend = jicurrP + nj;
                        for (; jicurrP != jiend; ++jicurrP) {
                            jicurrO = jointiinfosA[jiP+jicurrP];
                            final int infom = jicurrO.info.m;
                            int Arow = (mskip+1)*ofsi;//A + 
                            int JinvMrow = 2*8*ofsi;//JinvM + 
                            int Jrow = 2*8*ofsi;//J + 
                            Multiply2_p8r (A,Arow, JinvM,JinvMrow, J,Jrow, infom, infom, mskip);
                            if (jicurrO.joint.node[1].body!=null) {
                                MultiplyAdd2_p8r (A,Arow, JinvM,JinvMrow + 8*infom, J,Jrow + 8*infom, infom, infom, mskip);
                            }

                            ofsi += infom;
                        }
                    }

                    {
                        // add cfm to the diagonal of A
                        final int mskip = dPAD(m);

                        int Arow = 0;//A;
                        for (int i=0; i<m; Arow += mskip, ++i) {
                            A[Arow+i] += cfm[i] * stepsizeRecip;
                        }
                    }
                }

            } 
            memarena.END_STATE_SAVE(cfmstate);

            BlockPointer tmp1state = memarena.BEGIN_STATE_SAVE();
            {
                // compute the right hand side `rhs'
                IFTIMING_dTimerNow ("compute rhs");

                //dReal *tmp1 = memarena->AllocateArray<dReal> ((size_t)nb*8);
                memarena.dummy();
                double[] tmp1 = new double[nb*8];
                //dSetZero (tmp1,nb*8);

                {
                    // put v/h + invM*fe into tmp1
                    int tmp1currP = 0;//tmp1;
                    int invIrow = 0;//invI;
                    //            dxBody *const *const bodyend = body + nb;
                    //            for (dxBody *const *bodycurr = body; bodycurr != bodyend; tmp1curr+=8, invIrow+=12, ++bodycurr) {
                    //              dxBody *b = *bodycurr;
                    for (int ii = bOfs; ii < bOfs+nb; ii++, tmp1currP+=8, invIrow+=12) {
                        DxBody b = bodyA[ii];
                        //for (int j=0; j<3; ++j) tmp1[tmp1currP+j] = b.facc.get(j)*b.invMass + b.lvel.get(j)*stepsizeRecip;
                        tmp1[tmp1currP+0] = b.facc.get0()*b.invMass + b.lvel.get0()*stepsizeRecip;
                        tmp1[tmp1currP+1] = b.facc.get1()*b.invMass + b.lvel.get1()*stepsizeRecip;
                        tmp1[tmp1currP+2] = b.facc.get2()*b.invMass + b.lvel.get2()*stepsizeRecip;
                        dMultiply0_331 (tmp1,tmp1currP+4, invI,invIrow, b.tacc);
                        //for (int k=0; k<3; ++k) tmp1[tmp1currP+4+k] += b.avel.get(k)*stepsizeRecip;
                        tmp1[tmp1currP+4+0] += b.avel.get0()*stepsizeRecip;
                        tmp1[tmp1currP+4+1] += b.avel.get1()*stepsizeRecip;
                        tmp1[tmp1currP+4+2] += b.avel.get2()*stepsizeRecip;
                    }
                }

                {
                    // init rhs -- this erases 'c' as they reside in the same memory!!!
                    rhs = c;
                    for (int i=0; i<m; ++i) rhs[i] = c[i]*stepsizeRecip;
                    c = null; // set 'c' to NULL to prevent unexpected access
                }

                {
                    // put J*tmp1 into rhs
                    int ofsi = 0;
                    dJointWithInfo1 jicurrO = jointiinfosA[jiP];
                    int jicurrP = 0;
                    int jiend = jicurrP + nj;
                    for (; jicurrP != jiend; ++jicurrP) {
                        jicurrO = jointiinfosA[jiP+jicurrP];//TZ
                        final int infom = jicurrO.info.m;
                        DxJoint joint = jicurrO.joint;

                        int rhscurr = ofsi;//rhs+
                        int Jrow = 2*8*ofsi;//J + 
                        MultiplySub0_p81 (rhs,rhscurr, J,Jrow, tmp1, 8*joint.node[0].body.tag, infom);
                        if (joint.node[1].body!=null) {
                            MultiplySub0_p81 (rhs,rhscurr, J,Jrow + 8*infom, tmp1, 8*joint.node[1].body.tag, infom);
                        }

                        ofsi += infom;
                    }
                }
            } 
            memarena.END_STATE_SAVE(tmp1state);

            //dReal *lambda = memarena->AllocateArray<dReal> (m);
            memarena.dummy();
            double[] lambda = new double[m];

            BlockPointer lcpstate = memarena.BEGIN_STATE_SAVE();
            {
                IFTIMING_dTimerNow ("solving LCP problem");

                // solve the LCP problem and get lambda.
                // this will destroy A but that's OK
                DLCP.dSolveLCP (memarena, m, A, lambda, rhs, null, nub, lo, hi, findex);

            } 
            memarena.END_STATE_SAVE(lcpstate);

            {
                IFTIMING_dTimerNow ("compute constraint force");

                // compute the constraint force `cforce'
                // compute cforce = J'*lambda
                int ofsi = 0;
                dJointWithInfo1 jicurrO = jointiinfosA[jiP];
                int jicurrP = 0;
                int jiend = jicurrP + nj;
                for (; jicurrP != jiend; ++jicurrP) {
                    jicurrO = jointiinfosA[jiP+jicurrP];
                    final int infom = jicurrO.info.m;
                    DxJoint joint = jicurrO.joint;

                    int JJ = 2*8*ofsi;//J + 
                    int lambdarow = ofsi;//lambda + 

                    DJointFeedback fb = joint.feedback;

                    if (fb!=null) {
                        // the user has requested feedback on the amount of force that this
                        // joint is applying to the bodies. we use a slightly slower
                        // computation that splits out the force components and puts them
                        // in the feedback structure.
                        double[] data=new double[8];
                        Multiply1_8q1 (data, 0, J,JJ, lambda,lambdarow, infom);

                        DxBody b1 = joint.node[0].body;
                        int cf1 = 8*b1.tag;//cforce + 
                        fb.f1.set(data[0],data[1],data[2]);
                        fb.t1.set(data[4],data[5],data[6]);
                        cforce[cf1+0] += data[0];
                        cforce[cf1+1] += data[1];
                        cforce[cf1+2] += data[2];
                        cforce[cf1+4] += data[4];
                        cforce[cf1+5] += data[5];
                        cforce[cf1+6] += data[6]; 

                        DxBody b2 = joint.node[1].body;
                        if (b2!=null) {
                            Multiply1_8q1 (data,0, J,JJ + 8*infom, lambda,lambdarow, infom);

                            int cf2 = 8*b2.tag;//cforce + 
                            fb.f2.set(data[0],data[1],data[2]);
                            fb.t2.set(data[4],data[5],data[6]);
                            cforce[cf2+0] += data[0];
                            cforce[cf2+1] += data[1];
                            cforce[cf2+2] += data[2];
                            cforce[cf2+4] += data[4];
                            cforce[cf2+5] += data[5];
                            cforce[cf2+6] += data[6]; 
                       }
                    }
                    else {
                        // no feedback is required, let's compute cforce the faster way
                        DxBody b1 = joint.node[0].body;
                        int cf1 = 8*b1.tag;//cforce + 
                        MultiplyAdd1_8q1 (cforce,cf1, J,JJ, lambda,lambdarow, infom);

                        DxBody b2 = joint.node[1].body;
                        if (b2!=null) {
                            int cf2 = 8*b2.tag;//cforce + 
                            MultiplyAdd1_8q1 (cforce, cf2, J,JJ + 8*infom, lambda,lambdarow, infom);
                       }
                    }

                    ofsi += infom;
                }
            }
        } // if (m > 0)

        {
            // compute the velocity update
            IFTIMING_dTimerNow ("compute velocity update");

            // add fe to cforce and multiply cforce by stepsize
            double[] data = new double[4];
            int invIrowP = 0;//invI;
            int cforcecurrP = 0;//cforce;
//        dxBody *const *const bodyend = body + nb;
//        for (dxBody *const *bodycurr = body; bodycurr != bodyend; invIrow+=12, cforcecurr+=8, ++bodycurr) {
//          dxBody *b = *bodycurr;
            for (int ii = bOfs; ii < bOfs+nb; ii++, invIrowP+=12, cforcecurrP+=8) {
                DxBody b = bodyA[ii];
                
                double body_invMass_mul_stepsize = stepsize * b.invMass;
                //for (int j=0; j<3; ++j) b.lvel[j] += (cforcecurr[j] + b.facc[j]) * body_invMass_mul_stepsize;
                b.lvel.add0( (cforce[cforcecurrP+0] + b.facc.get0()) * body_invMass_mul_stepsize);
                b.lvel.add1( (cforce[cforcecurrP+1] + b.facc.get1()) * body_invMass_mul_stepsize);
                b.lvel.add2( (cforce[cforcecurrP+2] + b.facc.get2()) * body_invMass_mul_stepsize);

                //for (int k=0; k<3; ++k) data[k] = (cforcecurr[4+k] + b.tacc[k]) * stepsize;
                data[0] = (cforce[cforcecurrP+4+0] + b.tacc.get0()) * stepsize;
                data[1] = (cforce[cforcecurrP+4+1] + b.tacc.get1()) * stepsize;
                data[2] = (cforce[cforcecurrP+4+2] + b.tacc.get2()) * stepsize;
                dMultiplyAdd0_331 (b.avel, invI, invIrowP, data, 0);
            }
        }

        {
            // update the position and orientation from the new linear/angular velocity
            // (over the given timestep)
            IFTIMING_dTimerNow ("update position");
//            dxBody *const *const bodyend = body + nb;
//            for (dxBody *const *bodycurr = body; bodycurr != bodyend; ++bodycurr) {
//                dxBody *b = *bodycurr;
            for (int ii = bOfs; ii < bOfs+nb; ii++) {
                DxBody b = bodyA[ii];
                b.dxStepBody (stepsize);
            }
        }

        {
            IFTIMING_dTimerNow ("tidy up");

            // zero all force accumulators
//            dxBody *const *const bodyend = body + nb;
//            for (dxBody *const *bodycurr = body; bodycurr != bodyend; ++bodycurr) {
//                DxBody b = *bodycurr;
            for (int ii = bOfs; ii < bOfs+nb; ii++) {
                DxBody b = bodyA[ii];
//          b->facc[0] = 0;
//          b->facc[1] = 0;
//          b->facc[2] = 0;
//          b->facc[3] = 0;
//          b->tacc[0] = 0;
//          b->tacc[1] = 0;
//          b->tacc[2] = 0;
//          b->tacc[3] = 0;
          b.facc.setZero();
          b.tacc.setZero();
        }
      }

      IFTIMING_dTimerEnd();
      if (m > 0) IFTIMING_dTimerReport (stdout,1);

    }

    //****************************************************************************

//    void dInternalStepIsland (dxWorldProcessMemArena *memarena, 
//                              dxWorld *world, dxBody * const *body, unsigned int nb,
//                              dxJoint * const *joint, unsigned int nj, dReal stepsize)
	private final void dInternalStepIsland (DxWorldProcessMemArena memarena, 
	        DxWorld world, final DxBody[] bodyA, final int bOfs, final int nb,
	        DxJoint[] jointA, final int jOfs, final int nj, double stepsize)
	{
	    dInternalStepIsland_x2 (memarena,world,bodyA,bOfs,nb,jointA,jOfs,nj,stepsize);
	}

    int dxEstimateStepMemoryRequirements (DxBody[] body, int nb, DxJoint[] _joint, int _nj)
    {
//      int nj, m;
//
//      {
//        int njcurr = 0, mcurr = 0;
//        DxJoint.SureMaxInfo info;
//        DxJoint *const *const _jend = _joint + _nj;
//        for (dxJoint *const *_jcurr = _joint; _jcurr != _jend; ++_jcurr) {  
//          dxJoint *j = *_jcurr;
//          j.getSureMaxInfo (info);
//
//          int jm = info.max_m;
//          if (jm > 0) {
//            njcurr++;
//
//            mcurr += jm;
//          }
//        }
//        nj = njcurr; m = mcurr;
//      }
//
//      size_t res = 0;
//
//      res += dEFFICIENT_SIZE(sizeof(dReal) * 3 * 4 * (size_t)nb); // for invI
//
//      {
//        size_t sub1_res1 = dEFFICIENT_SIZE(sizeof(dJointWithInfo1) * 2 * (size_t)_nj); // for initial jointiinfos
//
//        // The array can't grow right more than by nj
//        size_t sub1_res2 = dEFFICIENT_SIZE(sizeof(dJointWithInfo1) * ((size_t)_nj + (size_t)nj)); // for shrunk jointiinfos
//        sub1_res2 += dEFFICIENT_SIZE(sizeof(dReal) * 8 * (size_t)nb); // for cforce
//        if (m > 0) {
//          sub1_res2 += dEFFICIENT_SIZE(sizeof(dReal) * 2 * 8 * (size_t)m); // for J
//          int mskip = dPAD(m);
//          sub1_res2 += dEFFICIENT_SIZE(sizeof(dReal) * (size_t)mskip * (size_t)m); // for A
//          sub1_res2 += 3 * dEFFICIENT_SIZE(sizeof(dReal) * (size_t)m); // for lo, hi, rhs
//          sub1_res2 += dEFFICIENT_SIZE(sizeof(int) * (size_t)m); // for findex
//          {
//            size_t sub2_res1 = dEFFICIENT_SIZE(sizeof(dReal) * (size_t)m); // for cfm
//            sub2_res1 += dEFFICIENT_SIZE(sizeof(dReal) * 2 * 8 * (size_t)m); // for JinvM
//            {
//              size_t sub3_res1 = dEFFICIENT_SIZE(sizeof(int) * (size_t)m); // for ofs
//
//              size_t sub3_res2 = 0;
//
//              sub2_res1 += (sub3_res1 >= sub3_res2) ? sub3_res1 : sub3_res2;
//            }
//
//            size_t sub2_res2 = 0;
//            {
//              size_t sub3_res1 = 0;
//              {
//                size_t sub4_res1 = dEFFICIENT_SIZE(sizeof(dReal) * 8 * (size_t)nb); // for tmp1
//
//                size_t sub4_res2 = 0;
//
//                sub3_res1 += (sub4_res1 >= sub4_res2) ? sub4_res1 : sub4_res2;
//              }
//
//              size_t sub3_res2 = dEFFICIENT_SIZE(sizeof(dReal) * (size_t)m); // for lambda
//              {
//                size_t sub4_res1 = dEstimateSolveLCPMemoryReq(m, false);
//
//                size_t sub4_res2 = 0;
//
//                sub3_res2 += (sub4_res1 >= sub4_res2) ? sub4_res1 : sub4_res2;
//              }
//
//              sub2_res2 += (sub3_res1 >= sub3_res2) ? sub3_res1 : sub3_res2;
//            }
//
//            sub1_res2 += (sub2_res1 >= sub2_res2) ? sub2_res1 : sub2_res2;
//          }
//        }
//
//        res += (sub1_res1 >= sub1_res2) ? sub1_res1 : sub1_res2;
//      }
//
//      return res;
        return -1;
	}

//	//****************************************************************************
//	// an optimized version of dInternalStepIsland1()
//
//	//void dInternalStepIsland_x2 (dxWorld *world, dxBody * const *body, int nb,
//	//	     dxJoint * const *_joint, int nj, double stepsize)
//	private void dInternalStepIsland_x2 (DxWorld world, final DxBody[] body, 
//			int nb,
//			final DxJoint[] _joint, int nj, double stepsize)
//	{
//		int i,j,k;
//		if (TIMING) //#ifdef TIMING
//			dTimerStart("preprocessing");
//		//#endif
//
//		double stepsize1 = dRecip(stepsize);
//
//		// number all bodies in the body list - set their tag values
//		for (i=0; i<nb; i++) body[i].tag = i;
//
//		// make a local copy of the joint array, because we might want to modify it.
//		// (the "dxJoint *const*" declaration says we're allowed to modify the joints
//		// but not the joint array, because the caller might need it unchanged).
//		DxJoint[] joint = new DxJoint[nj];//ALLOCA(dxJoint*,joint,nj*sizeof(dxJoint*));
//		System.arraycopy(_joint, 0, joint, 0, nj);
//		//memcpy (joint,_joint,nj);// * sizeof(dxJoint*));
//
//		// for all bodies, compute the inertia tensor and its inverse in the global
//		// frame, and compute the rotational force and add it to the torque
//		// accumulator. invI are vertically stacked 3x4 matrices, one per body.
//		// @@@ check computation of rotational force.
//
//		double[] invI = new double[3*nb*4];//ALLOCA(double,invI,3*nb*4*sizeof(double));
//
//		//dSetZero (I,3*nb*4);
//		//dSetZero (invI,3*nb*4);
//		//TZ:
//		DMatrix3 tmpM = new DMatrix3();
//		DVector3 tmpV = new DVector3();
//		DMatrix3 tmpI = new DMatrix3();
//		for (i=0; i<nb; i++) {
//			//TZ double[] tmp=new double[12];
//
//			// compute inverse inertia tensor in global frame
//			dMultiply2_333 (tmpM,body[i].invI,body[i].posr().R());
//			dMultiply0_333 (invI,i*12,body[i].posr().R(),tmpM);
//
//		    if (body[i].isFlagsGyroscopic()) {
//		        //TZ move up for performance 
//		    	//DMatrix3 I = new DMatrix3();
//				// compute inertia tensor in global frame
//				dMultiply2_333 (tmpM,body[i].mass._I,body[i].posr().R());
//				dMultiply0_333 (tmpI,body[i].posr().R(),tmpM);
//
//				// compute rotational force
//				dMultiply0_331 (tmpV,tmpI,body[i].avel);
//				dSubtractVectorCross3 (body[i].tacc,body[i].avel,tmpV);
//			}
//		}
//
//		// add the gravity force to all bodies
//		for (i=0; i<nb; i++) {
//			if ((body[i].flags & DxBody.dxBodyNoGravity)==0) {
////				body[i].facc.v[0] += body[i].mass._mass * world.gravity.v[0];
////				body[i].facc.v[1] += body[i].mass._mass * world.gravity.v[1];
////				body[i].facc.v[2] += body[i].mass._mass * world.gravity.v[2];
//				body[i].facc.eqSum(body[i].facc, world.gravity, body[i].mass._mass);
//			}
//		}
//
//		// get m = total constraint dimension, nub = number of unbounded variables.
//		// create constraint offset array and number-of-rows array for all joints.
//		// the constraints are re-ordered as follows: the purely unbounded
//		// constraints, the mixed unbounded + LCP constraints, and last the purely
//		// LCP constraints. this assists the LCP solver to put all unbounded
//		// variables at the start for a quick factorization.
//		//
//		// joints with m=0 are inactive and are removed from the joints array
//		// entirely, so that the code that follows does not consider them.
//		// also number all active joints in the joint list (set their tag values).
//		// inactive joints receive a tag value of -1.
//
//		int m = 0;
//		DxJoint.Info1[] info = new DxJoint.Info1[nj];//ALLOCA(dxJoint.Info1,info,nj*sizeof(dxJoint.Info1));
//		for (int ii = 0; ii < info.length; ii++) info[ii] = new DxJoint.Info1();
//		int[] ofs = new int[nj];//ALLOCA(int,ofs,nj*sizeof(int));
//		for (i=0, j=0; j<nj; j++) {	// i=dest, j=src
//			joint[j].getInfo1 (info[i]);
//			dIASSERT (info[i].m >= 0 && info[i].m <= 6 &&
//					info[i].nub >= 0 && info[i].nub <= info[i].m);
//			if (info[i].m > 0) {
//				joint[i] = joint[j];
//				joint[i].tag = i;
//				i++;
//			}
//			else {
//				joint[j].tag = -1;
//			}
//		}
//		nj = i;
//
//		// the purely unbounded constraints
//		for (i=0; i<nj; i++) if (info[i].nub == info[i].m) {
//			ofs[i] = m;
//			m += info[i].m;
//		}
//		int nub = m;
//		// the mixed unbounded + LCP constraints
//		for (i=0; i<nj; i++) if (info[i].nub > 0 && info[i].nub < info[i].m) {
//			ofs[i] = m;
//			m += info[i].m;
//		}
//		// the purely LCP constraints
//		for (i=0; i<nj; i++) if (info[i].nub == 0) {
//			ofs[i] = m;
//			m += info[i].m;
//		}
//
//		// this will be set to the force due to the constraints
//		double[] cforce = new double[nb*8];//ALLOCA(double,cforce,nb*8*sizeof(double));
//		//TZ dSetZero (cforce,nb*8);
//
//		// if there are constraints, compute cforce
//		if (m > 0) {
//			// create a constraint equation right hand side vector `c', a constraint
//			// force mixing vector `cfm', and LCP low and high bound vectors, and an
//			// 'findex' vector.
//			double[] c = new double[m];//ALLOCA(double,c,m*sizeof(double));
//			double[] cfm = new double[m];//ALLOCA(double,cfm,m*sizeof(double));
//			double[] lo = new double[m];//ALLOCA(double,lo,m*sizeof(double));
//			double[] hi = new double[m];//ALLOCA(double,hi,m*sizeof(double));
//			int[] findex = new int[m];//ALLOCA(int,findex,m*sizeof(int));
//			//TZ dSetZero (c,m);
//			dSetValue (cfm,m,world.getCFM());
//			dSetValue (lo,m,-dInfinity);
//			dSetValue (hi,m, dInfinity);
//			for (i=0; i<m; i++) findex[i] = -1;
//
//			// get jacobian data from constraints. a (2*m)x8 matrix will be created
//			// to store the two jacobian blocks from each constraint. it has this
//			// format:
//			//
//			//   l l l 0 a a a 0  \    .
//			//   l l l 0 a a a 0   }-- jacobian body 1 block for joint 0 (3 rows)
//			//   l l l 0 a a a 0  /
//			//   l l l 0 a a a 0  \    .
//			//   l l l 0 a a a 0   }-- jacobian body 2 block for joint 0 (3 rows)
//			//   l l l 0 a a a 0  /
//			//   l l l 0 a a a 0  }--- jacobian body 1 block for joint 1 (1 row)
//			//   l l l 0 a a a 0  }--- jacobian body 2 block for joint 1 (1 row)
//			//   etc...
//			//
//			//   (lll) = linear jacobian data
//			//   (aaa) = angular jacobian data
//			//
//			//#   ifdef TIMING
//			if (TIMING)
//				dTimerNow ("create J");
//			//#   endif
//			double[] J = new double[2*m*8];//ALLOCA(double,J,2*m*8*sizeof(double));
//			//TZ dSetZero (J,2*m*8);
//			DxJoint.Info2 Jinfo = new DxJoint.Info2();
//			Jinfo.setRowskip(8);
//			Jinfo.setArrays(J, c, cfm, lo, hi, findex);
//			Jinfo.fps = stepsize1;
//			Jinfo.erp = world.getERP();
//			for (i=0; i<nj; i++) {
//				Jinfo.J1lp = 2*8*ofs[i];//J + 2*8*ofs[i];
//				Jinfo.J1ap = Jinfo.J1lp + 4;
//				Jinfo.J2lp = Jinfo.J1lp + 8*info[i].m;
//				Jinfo.J2ap = Jinfo.J2lp + 4;
////				Jinfo.c = c + ofs[i];
////				Jinfo.cfm = cfm + ofs[i];
////				Jinfo.lo = lo + ofs[i];
////				Jinfo.hi = hi + ofs[i];
////				Jinfo.findex = findex + ofs[i];
//				Jinfo.setAllP(ofs[i]);
//				joint[i].getInfo2 (Jinfo);
//
//
//				// adjust returned findex values for global index numbering
//				for (j=0; j<info[i].m; j++) {
//					if (findex[ofs[i] + j] >= 0) findex[ofs[i] + j] += ofs[i];
//				}
//			}
//
//			// compute A = J*invM*J'. first compute JinvM = J*invM. this has the same
//			// format as J so we just go through the constraints in J multiplying by
//			// the appropriate scalars and matrices.
//			//#   ifdef TIMING
//			if (TIMING)
//				dTimerNow ("compute A");
//			//#   endif
//			double[] JinvM = new double[2*m*8];//ALLOCA(double,JinvM,2*m*8*sizeof(double));
//			//TZ dSetZero (JinvM,2*m*8);
//			for (i=0; i<nj; i++) {
//				int b = joint[i].node[0].body.tag;
//				double body_invMass = body[b].invMass;
////				double *body_invI = invI + b*12;
////				double *Jsrc = J + 2*8*ofs[i];
////				double *Jdst = JinvM + 2*8*ofs[i];
//				int body_invI = b*12; //invI + b*12;
//				int Jsrc = 2*8*ofs[i];//J + 2*8*ofs[i];
//				int Jdst = 2*8*ofs[i]; //JinvM + 2*8*ofs[i];
//				for (j=info[i].m-1; j>=0; j--) {
//					//for (k=0; k<3; k++) Jdst[k] = Jsrc[k] * body_invMass;
//					for (k=0; k<3; k++) JinvM[Jdst+k] = J[Jsrc+k] * body_invMass;
//					dMultiply0_133 (JinvM,Jdst+4,J,Jsrc+4,invI,body_invI);
//					Jsrc += 8;
//					Jdst += 8;
//				}
//				if (joint[i].node[1].body!=null) {
//					b = joint[i].node[1].body.tag;
//					body_invMass = body[b].invMass;
//					body_invI = b*12;//invI + b*12;
//					for (j=info[i].m-1; j>=0; j--) {
//						//for (k=0; k<3; k++) Jdst[k] = Jsrc[k] * body_invMass;
//						for (k=0; k<3; k++) JinvM[Jdst+k] = J[Jsrc+k] * body_invMass;
//						dMultiply0_133 (JinvM,Jdst+4,J,Jsrc+4,invI,body_invI);
//						Jsrc += 8;
//						Jdst += 8;
//					}
//				}
//			}
//
//			// now compute A = JinvM * J'. A's rows and columns are grouped by joint,
//			// i.e. in the same way as the rows of J. block (i,j) of A is only nonzero
//			// if joints i and j have at least one body in common. this fact suggests
//			// the algorithm used to fill A:
//			//
//			//    for b = all bodies
//			//      n = number of joints attached to body b
//			//      for i = 1..n
//			//        for j = i+1..n
//			//          ii = actual joint number for i
//			//          jj = actual joint number for j
//			//          // (ii,jj) will be set to all pairs of joints around body b
//			//          compute blockwise: A(ii,jj) += JinvM(ii) * J(jj)'
//			//
//			// this algorithm catches all pairs of joints that have at least one body
//			// in common. it does not compute the diagonal blocks of A however -
//			// another similar algorithm does that.
//
//			int mskip = dPAD(m);
//			double[] A = new double[m*mskip];//ALLOCA(double,A,m*mskip*sizeof(double));
//			//TZ dSetZero (A,m*mskip);
//			for (i=0; i<nb; i++) {
//				//        for (dxJointNode *n1=body[i].firstjoint; n1!=null; n1=n1.next) {
//				for (DxJointNode n1=body[i].firstjoint.get(); n1!=null; n1=n1.next) {
//					//    	  for (dxJointNode *n2=n1.next; n2; n2=n2.next) {
//					for (DxJointNode n2=n1.next; n2!=null; n2=n2.next) {
//						// get joint numbers and ensure ofs[j1] >= ofs[j2]
//						int j1 = n1.joint.tag;
//						int j2 = n2.joint.tag;
//						if (ofs[j1] < ofs[j2]) {
//							int tmp = j1;
//							j1 = j2;
//							j2 = tmp;
//						}
//
//						// if either joint was tagged as -1 then it is an inactive (m=0)
//						// joint that should not be considered
//						if (j1==-1 || j2==-1) continue;
//
//						// determine if body i is the 1st or 2nd body of joints j1 and j2
//						int jb1 = (joint[j1].node[1].body == body[i]) ? 1 : 0;
//						int jb2 = (joint[j2].node[1].body == body[i]) ? 1 : 0;
//						// jb1/jb2 must be 0 for joints with only one body
//						dIASSERT(joint[j1].node[1].body!=null || jb1==0);
//						dIASSERT(joint[j2].node[1].body!=null || jb2==0);
//
//						// set block of A
//						MultiplyAdd2_p8r (A , ofs[j1]*mskip + ofs[j2],
//								JinvM , 2*8*ofs[j1] + jb1*8*info[j1].m,
//								J     , 2*8*ofs[j2] + jb2*8*info[j2].m,
//								info[j1].m,info[j2].m, mskip);
//					}
//				}
//			}
//			// compute diagonal blocks of A
//			for (i=0; i<nj; i++) {
//				Multiply2_p8r (A , ofs[i]*(mskip+1),
//						JinvM , 2*8*ofs[i],
//						J , 2*8*ofs[i],
//						info[i].m,info[i].m, mskip);
//				if (joint[i].node[1].body!=null) {
//					MultiplyAdd2_p8r (A , ofs[i]*(mskip+1),
//							JinvM , 2*8*ofs[i] + 8*info[i].m,
//							J , 2*8*ofs[i] + 8*info[i].m,
//							info[i].m,info[i].m, mskip);
//				}
//			}
//
//			// add cfm to the diagonal of A
//			for (i=0; i<m; i++) A[i*mskip+i] += cfm[i] * stepsize1;
//
//			//#   ifdef COMPARE_METHODS
////TODO remove			if (COMPARE_METHODS)
////				comparator.nextMatrix (A,m,m,1,"A");
//			//#   endif
//
//			// compute the right hand side `rhs'
//			//#   ifdef TIMING
//			if (TIMING)
//				dTimerNow ("compute rhs");
//			//#   endif
//			double[] tmp1 = new double[nb*8];//ALLOCA(double,tmp1,nb*8*sizeof(double));
//			//dSetZero (tmp1,nb*8);
//			// put v/h + invM*fe into tmp1
//			for (i=0; i<nb; i++) {
//				double body_invMass = body[i].invMass;
//				int body_invI = i*12;//double []body_invI = invI + i*12;
//				for (j=0; j<3; j++) tmp1[i*8+j] = 
//					body[i].facc.get(j) * body_invMass +
//					body[i].lvel.get(j) * stepsize1;
//				dMultiply0_331 (tmp1 , i*8 + 4,invI,body_invI,body[i].tacc);
//				for (j=0; j<3; j++) tmp1[i*8+4+j] += body[i].avel.get(j) * stepsize1;
//			}
//			// put J*tmp1 into rhs
//			double[]rhs = new double[m];//ALLOCA(double,rhs,m*sizeof(double));
//			//dSetZero (rhs,m);
//			for (i=0; i<nj; i++) {
//				int JJ = 2*8*ofs[i];//double []JJ = J + 2*8*ofs[i];
//				Multiply0_p81 (rhs,ofs[i],J,JJ,
//						tmp1 , 8*joint[i].node[0].body.tag, info[i].m);
//				if (joint[i].node[1].body!=null) {
//					MultiplyAdd0_p81 (rhs,ofs[i],J, JJ + 8*info[i].m,
//							tmp1 , 8*joint[i].node[1].body.tag, info[i].m);
//				}
//			}
//			// complete rhs
//			for (i=0; i<m; i++) rhs[i] = c[i]*stepsize1 - rhs[i];
//
//			//#   ifdef COMPARE_METHODS
////TODO remove			if (COMPARE_METHODS) {
////				comparator.nextMatrix (c,m,1,0,"c");
////				comparator.nextMatrix (rhs,m,1,0,"rhs");
////			}//#   endif
//
//			// solve the LCP problem and get lambda.
//			// this will destroy A but that's okay
//			//#   ifdef TIMING
//			if (TIMING)
//				dTimerNow ("solving LCP problem");
//			//#   endif
//			double[] lambda = new double[m];//ALLOCA(double,lambda,m*sizeof(double));
//			double[] residual = new double[m];//ALLOCA(double,residual,m*sizeof(double));
//			DLCP.dSolveLCP (m,A,lambda,rhs,residual,nub,lo,hi,findex);
//
//			//TODO TZ
//			//#ifdef dUSE_MALLOC_FOR_ALLOCA
//			//    if (dMemoryFlag == d_MEMORY_OUT_OF_MEMORY)
//			//      return;
//			//#endif
//
//
//			//  OLD WAY - direct factor and solve
//			//
//			//    // factorize A (L*L'=A)
//			//#   ifdef TIMING
//			//    dTimerNow ("factorize A");
//			//#   endif
//			//    dReal *L = (dReal*) ALLOCA (m*mskip*sizeof(dReal));
//			//    memcpy (L,A,m*mskip*sizeof(dReal));
//			//#   ifdef FAST_FACTOR
//			//    dFastFactorCholesky (L,m);  // does not report non positive definiteness
//			//#   else
//			//    if (dFactorCholesky (L,m)==0) dDebug (0,"A is not positive definite");
//			//#   endif
//			//
//			//    // compute lambda
//			//#   ifdef TIMING
//			//    dTimerNow ("Timer.dTimere lambda");
//			//#   endif
//			//    dReal *lambda = (dReal*) ALLOCA (m * sizeof(dReal));
//			//    memcpy (lambda,rhs,m * sizeof(dReal));
//			//    dSolveCholesky (L,lambda,m);
//
//			//#   ifdef COMPARE_METHODS
////TODO remove			if (COMPARE_METHODS)
////				comparator.nextMatrix (lambda,m,1,0,"lambda");
//			//#   endif
//
//			// compute the constraint force `cforce'
//			//#   ifdef TIMING
//			if (TIMING)
//				dTimerNow ("compute constraint force");
//			//#   endif
//			// compute cforce = J'*lambda
//			for (i=0; i<nj; i++) {
//				int JJ = 2*8*ofs[i];//double[] JJ = J + 2*8*ofs[i];
//				DxBody b1 = joint[i].node[0].body;
//				DxBody b2 = joint[i].node[1].body;
//				DJoint.DJointFeedback fb = joint[i].feedback;
//
//				if (fb!=null) {
//					// the user has requested feedback on the amount of force that this
//					// joint is applying to the bodies. we use a slightly slower
//					// computation that splits out the force components and puts them
//					// in the feedback structure.
//					double[] data=new double[8];
//
//					Multiply1_8q1 (data,0, J,JJ, lambda,ofs[i], info[i].m);
//					int cf1 = 8*b1.tag;//double[] cf1 = cforce + 8*b1.tag;
//					fb.f1.set(0,data[0]);
//                    fb.f1.set(1,data[1]);
//                    fb.f1.set(2,data[2]);
//                    fb.t1.set(0,data[4]);
//                    fb.t1.set(1,data[5]);
//                    fb.t1.set(2,data[6]);
//                    cforce[cf1+0] += data[0];
//                    cforce[cf1+1] += data[1];
//                    cforce[cf1+2] += data[2];
//                    cforce[cf1+4] += data[4];
//                    cforce[cf1+5] += data[5];
//                    cforce[cf1+6] += data[6]; 
//					if (b2!=null){
//						Multiply1_8q1 (data,0, J,JJ + 8*info[i].m, lambda,ofs[i], info[i].m);
//						int cf2 = 8*b2.tag;//double[] cf2 = cforce + 8*b2.tag;
//	                    fb.f2.set(0,data[0]);
//	                    fb.f2.set(1,data[1]);
//	                    fb.f2.set(2,data[2]);
//	                    fb.t2.set(0,data[4]);
//	                    fb.t2.set(1,data[5]);
//	                    fb.t2.set(2,data[6]);
//	                    cforce[cf2+0] += data[0];
//	                    cforce[cf2+1] += data[1];
//	                    cforce[cf2+2] += data[2];
//	                    cforce[cf2+4] += data[4];
//	                    cforce[cf2+5] += data[5];
//	                    cforce[cf2+6] += data[6]; 
//					}
//				}
//				else {
//					// no feedback is required, let's compute cforce the faster way
//					MultiplyAdd1_8q1 (cforce , 8*b1.tag,J,JJ, lambda,ofs[i], 
//							info[i].m);
//					if (b2!=null) {
//						MultiplyAdd1_8q1 (cforce , 8*b2.tag,
//								J,JJ + 8*info[i].m, lambda,ofs[i], info[i].m);
//					}
//				}
//			}
//		}
//
//		// compute the velocity update
//		//#ifdef TIMING
//		if (TIMING)
//			dTimerNow ("compute velocity update");
//		//#endif
//
//		// add fe to cforce
//		for (i=0; i<nb; i++) {
//			for (j=0; j<3; j++) cforce[i*8+j] += body[i].facc.get(j);
//			for (j=0; j<3; j++) cforce[i*8+4+j] += body[i].tacc.get(j);
//		}
//		// multiply cforce by stepsize
//		for (i=0; i < nb*8; i++) cforce[i] *= stepsize;
//		// add invM * cforce to the body velocity
//		for (i=0; i<nb; i++) {
//			double body_invMass = body[i].invMass;
//			int body_invI = i*12;//double []body_invI = invI + i*12;
//			for (j=0; j<3; j++) body[i].lvel.add(j, body_invMass * cforce[i*8+j] );
//			dMultiplyAdd0_331 (body[i].avel,invI,body_invI,cforce,i*8+4);
//		}
//
//		// update the position and orientation from the new linear/angular velocity
//		// (over the given timestep)
//		//# ifdef TIMING
//		if (TIMING)
//			dTimerNow ("update position");
//		//# endif
//		for (i=0; i<nb; i++) body[i].dxStepBody (stepsize);
//
//		//#ifdef COMPARE_METHODS
////TODO remove		if (COMPARE_METHODS) {
////			double[] tmp = new double[nb*6];//ALLOCA(double,tmp, nb*6*sizeof(double));
////			for (i=0; i<nb; i++) {
////				for (j=0; j<3; j++) tmp_vnew[i*6+j] = body[i].lvel.v[j];
////				for (j=0; j<3; j++) tmp_vnew[i*6+3+j] = body[i].avel.v[j];
////			}
////			comparator.nextMatrix (tmp_vnew,nb*6,1,0,"vnew");
////		}//#endif
//
//		//#ifdef TIMING
//		if (TIMING)
//			dTimerNow ("tidy up");
//		//#endif
//
//		// zero all force accumulators
//		for (i=0; i<nb; i++) {
//			body[i].facc.setZero();
//			body[i].tacc.setZero();
////			body[i].facc[0] = 0;
////			body[i].facc[1] = 0;
////			body[i].facc[2] = 0;
////			body[i].facc[3] = 0;
////			body[i].tacc[0] = 0;
////			body[i].tacc[1] = 0;
////			body[i].tacc[2] = 0;
////			body[i].tacc[3] = 0;
//		}
//
//		//#ifdef TIMING
//		if (TIMING) {
//			dTimerEnd();
//			if (m > 0) dTimerReport (stdout,1);
//		} //#endif
//
//	}
//
//	
//	//****************************************************************************
//
//	//void dInternalStepIsland (dxWorld *world, dxBody * const *body, int nb,
//	//			  dxJoint * const *joint, int nj, double stepsize)
//	void dInternalStepIsland (DxWorldProcessMemArena memarena, 
//	        DxWorld world, DxBody[] body, int nb,
//			DxJoint[] joint, int nj, double stepsize)
//	{
//
//		//TODO TZ
//		//#ifdef dUSE_MALLOC_FOR_ALLOCA
//		//  dMemoryFlag = d_MEMORY_OK;
//		//#endif
//
//		//#ifndef COMPARE_METHODS
////TODO remove		if (!COMPARE_METHODS) {
//			dInternalStepIsland_x2 (world,body,nb,joint,nj,stepsize);
//
//			//TODO TZ
//			//#ifdef dUSE_MALLOC_FOR_ALLOCA
//			//    if (dMemoryFlag == d_MEMORY_OUT_OF_MEMORY) {
//			//      REPORT_OUT_OF_MEMORY;
//			//      return;
//			//    }
//			//#endif
//
////		}//#endif
//
//		//#ifdef COMPARE_METHODS
////TODO remove		if (COMPARE_METHODS) {
////			int i;
////
////			// save body state
////			dxBody[] state = new dxBody[nb];//ALLOCA(dxBody,state,nb*sizeof(dxBody));
////
////			for (i=0; i<nb; i++) 
////				state[i] = (dxBody)body[i].clone();//memcpy (state+i,body[i],sizeof(dxBody));
////
////			// take slow step
////			comparator.reset();
////			dInternalStepIsland_x1 (world,body,nb,joint,nj,stepsize);
////			comparator.end();
////			//TODO TZ
////			//#ifdef dUSE_MALLOC_FOR_ALLOCA
////			//  if (dMemoryFlag == d_MEMORY_OUT_OF_MEMORY) {
////			//    REPORT_OUT_OF_MEMORY;
////			//    return;
////			//  }
////			//#endif
////
////			// restore state
////			for (i=0; i<nb; i++)  
////				state[i] = (dxBody)body[i].clone();//memcpy (body[i],state+i,sizeof(dxBody));
////
////			// take fast step
////			dInternalStepIsland_x2 (world,body,nb,joint,nj,stepsize);
////			comparator.end();
////			//TODO TZ
////			//#ifdef dUSE_MALLOC_FOR_ALLOCA
////			//    if (dMemoryFlag == d_MEMORY_OUT_OF_MEMORY) {
////			//      REPORT_OUT_OF_MEMORY;
////			//      return;
////			//    }
////			//#endif
////
////			//comparator.dump();
////			//_exit (1);
////		} //#endif COMPARE_METHODS
//	}


    @Override
    public int dxEstimateMemoryRequirements(DxBody[] body, int bodyOfs, int nb,
            DxJoint[] _joint, int jointOfs, int _nj) {
        // TODO Auto-generated method stub
        return 0;
    }


    @Override
    public void run(DxWorldProcessMemArena memarena, DxWorld world,
            DxBody[] body, int bodyOfs, int nb, DxJoint[] _joint, int jointOfs,
            int nj, double stepsize) {
        dInternalStepIsland(memarena, world, body, bodyOfs, nb, 
                _joint, jointOfs, nj, stepsize);
    }
}

