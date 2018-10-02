/*
 * AxisScalePolicyAutomaticBestFit.java of project jchart2d, <enterpurposehere>.
 * Copyright (C) 2002 - 2013, Achim Westermann, created on Apr 22, 2011
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version. This library is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 * 
 * If you modify or optimize the code in a useful way please let me know.
 * Achim.Westermann@gmx.de
 * 
 * 
 * File : $Source: /cvsroot/jchart2d/jchart2d/codetemplates.xml,v $ Date :
 * $Date: 2009/02/24 16:45:41 $ Version: $Revision: 1.2 $
 */

package info.monitorenter.gui.chart.axis.scalepolicy;

import info.monitorenter.gui.chart.IAxis;
import info.monitorenter.gui.chart.IAxisLabelFormatter;
import info.monitorenter.gui.chart.IAxisScalePolicy;
import info.monitorenter.gui.chart.LabeledValue;
import info.monitorenter.gui.chart.axis.AAxis;
import info.monitorenter.util.Range;
import info.monitorenter.util.math.MathUtil;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

/**
 * Very basic and fast scale policy implementation that ensures the following:
 * <ul>
 * <li>Every scale tick is a minor or major tick of the corresponding axis.</li>
 * <li>If a scale tick was found that matches a major and a minor tick it is
 * judged as major tick.</li>
 * <li>Every major tick is a multiple of minor ticks: It is not possible for the
 * sum minor ticks to "skip" a major tick.</li>
 * <li>There is no guarantee that the labels of ticks will overwrite each
 * others.</li>
 * <li>There is no guarantee that the major and minor ticks of the axis are
 * chosen in a reasonable manner: You could get no labels at all if the values
 * are too high or thousands of labels with a weird output.</li>
 * </ul>
 * <p>
 * 
 * 
 * 
 * @author <a href="mailto:Achim.Westermann@gmx.de">Achim Westermann </a>
 * 
 */

public class AxisScalePolicyAutomaticBestFit implements IAxisScalePolicy {
  /**
   * Internally used for rounding to ticks, calculated once per paint iteration.
   */
  // protected double m_power;

  public List<LabeledValue> getScaleValues(final Graphics2D g2d, final IAxis< ? > axis) {
    final double labelspacepx = axis.getAccessor().getMinimumValueDistanceForLabels(g2d);
    final double formattingspace = axis.getFormatter().getMinimumValueShiftForChange();
    // final double max = this.normalize(Math.max(labelspacepx,
    // formattingspace));
    final double max = Math.max(labelspacepx, formattingspace);
    return this.getLabels(max, axis);
  }

  /**
   * @see info.monitorenter.gui.chart.IAxisScalePolicy#initPaintIteration(info.monitorenter.gui.chart.IAxis)
   */
  public void initPaintIteration(IAxis< ? > axis) {
    /*
     * Get the powers of ten of the range, a minor Tick of 1.0 has to be able to
     * be 100 times in a range of 100 (match 1,2,3,... instead of 10,20,30,.. .
     */
    final double range = axis.getMax() - axis.getMin();
    double computeRange = range;
    if ((range == 0) || !MathUtil.isDouble(range)) {
      computeRange = 1;
    }
    double tmpPower = 0;
    if (computeRange > 1) {
      while (computeRange > 50) {
        computeRange /= 10;
        tmpPower++;
      }
      tmpPower = Math.pow(10, tmpPower - 1);

    } else {
      while (computeRange < 5) {
        computeRange *= 10;
        tmpPower++;
        if (Double.isInfinite(computeRange)) {
          computeRange = 0;
        }
      }

      tmpPower = 1 / Math.pow(10, tmpPower);
    }
    // this.m_power = tmpPower;

  }

  /**
   * Returns the labels for this axis.
   * <p>
   * The labels will have at least the given argument <code>resolution</code> as
   * distance in the value domain of the chart.
   * <p>
   * 
   * @param resolution
   *          the distance in the value domain of the chart that has to be at
   *          least between to labels.
   * 
   * @return the labels for the axis.
   */
  protected List<LabeledValue> getLabels(final double resolution, final IAxis< ? > axis) {
    final List<LabeledValue> collect = new LinkedList<LabeledValue>();
    if (resolution > 0) {

      final Range domain = axis.getRange();
      final double min = domain.getMin();
      final double max = domain.getMax();
      String oldLabelName = "";
      LabeledValue label;
      final double range = max - min;
      /*
       * Try to stick to ticks and be able to hit major ticks: Major tick
       * spacing should be a multiple of increment.
       */
      double minorTick = axis.getMinorTickSpacing();
      double majorTick = axis.getMajorTickSpacing();
      double increment = this.glueToTicks(minorTick, majorTick, resolution);

      final double tickResolution = this.roundToTicks(resolution, false, axis.isStartMajorTick(), axis).getValue();
      double value = Math.ceil(min / tickResolution) * tickResolution;
      // This was value before the patch that prevents the labels from jumping:
      // It's benefit was that the first label was not this
      // far from the start of data (in case startMajorTicks of axis is true):
      // double value = min;
      String labelName = "start";
      int loopStop = 0;
      boolean firstMajorFound = false;
      // first tick, manual init
      while ((value <= max) && (loopStop < 100)) {
        if (loopStop == 99) {
          if (AAxis.DEBUG) {
            System.out.println(axis.getAccessor().toString() + " axis: loop to high");
          }// value 1.3518972E12
        }
        label = this.roundToTicks(value, false, !firstMajorFound && axis.isStartMajorTick(), axis);

        oldLabelName = labelName;
        labelName = label.getLabel();
        value = label.getValue();

        loopStop++;
        if (firstMajorFound || !axis.isStartMajorTick() || label.isMajorTick()) {
          firstMajorFound = true;
          if ((value <= max) && (value >= min)) {
            if (oldLabelName.equals(labelName)) {
              if (AAxis.DEBUG) {
                System.out.println("constant Label " + labelName);
              }
            } else {
              collect.add(label);
            }
          } else if (value > max) {
            if (AAxis.DEBUG) {
              System.out.println("Dropping label (too high) : (" + label + ")[max: " + max + "]");
            }
          } else if (value < min) {
            if (AAxis.DEBUG) {
              System.out.println("Dropping label (too low) : (" + label + ")[min: " + min + "]");
            }
          }
        }
        value += increment;
      }
      final int stop = collect.size();

      for (int i = 0; i < stop; i++) {
        label = collect.get(i);
        label.setValue((label.getValue() - min) / range);
      }
    }
    return collect;
  }

  /**
   * Computes the needed increase of value per label.
   * 
   * Under the precondition:
   * 
   * <ul>
   * <li><code>majorTick is a multiple of minorTick</code></li>
   * </ul>
   * 
   * the following contract will be fulfilled:
   * 
   * <ul>
   * <li><code>result >= resolution</code></li>
   * <li>result is a multiple (including times 1) of minorTick</li>
   * <li>result may be a multiple (excluding times 1) of minorTick in case
   * majorTick will still be a multiple of result. This means: Every possible
   * majorTick will be hit. Note that if <code>resolution > majorTick</code> not
   * every majorTick must be hit. But then every nth majorTick that makes
   * <code>n*majorTick > resolution</code></li>
   * </ul>
   * <p>
   * In human language:
   * <ul>
   * <li>No increment will be returned that is smaller than the given
   * resolution.</li>
   * <li>It is tried to return the smallest multiple of minorTick that is bigger
   * resolution and still guarantees that no majorTick will be skipped.</li>
   * </ul>
   * <p>
   * 
   * 
   * 
   * @param minorTick
   *          the minorTicks of this scale policy.
   *          
   * @param majorTick
   *          the majorTicks of this scale policy.
   *          
   * @param resolution
   *          the minimum increment that is required for the scale (results from
   *          display space calculations).
   * 
   * @return The minimal increment per scale label that will try to hit each
   *         majorTick and to be as small as possible to also hit minorTicks
   *         (without skipping majorTicks).
   */
  private double glueToTicks(final double minorTick, final double majorTick, final double resolution) {
    double result = 0;
    double ratio = minorTick / resolution;
    if (ratio >= 1) {
      int capacity = (int) Math.floor(ratio);
      result = minorTick / capacity;
    } else {
      /*
       * Skip major ticks until they are at least greater than resolution. Only
       * pick possible values that are divisors of majorTick (or multiples in
       * case value grew bigger than major tick).
       */
      double shiftedResolution = minorTick;
      while ((shiftedResolution < resolution) || ((majorTick % shiftedResolution != 0) && (shiftedResolution % majorTick != 0))) {
        shiftedResolution += minorTick;
      }
      /*
       * Then find the resolution where it will hit at least some majort ticks
       * (we skipped some above).
       */
      if (shiftedResolution < majorTick) {
        result = shiftedResolution;
      } else {
        result = this.glueToTicks(shiftedResolution, majorTick, resolution);
      }
    }
    return result;
  }

  /**
   * Internal rounding routine.
   * <p>
   * Arguments are not chosen to be "understandable" or "usable" but optimized
   * for performance.
   * <p>
   * The <code> findMajorTick</code> argument may be used e.g. to force labels
   * to start from a major tick.
   * <p>
   * 
   * @param value
   *          the value to round.
   * 
   * @param floor
   *          if true, rounding goes to floor else to ceiling.
   * 
   * @param findMajorTick
   *          if true the returned value will be a major tick (which might be
   *          fare more away from the given value than the next major tick).
   * 
   * @return the value rounded to minor or major ticks.
   */
  protected LabeledValue roundToTicks(final double value, final boolean floor, final boolean findMajorTick, final IAxis< ? > axis) {
    final LabeledValue ret = new LabeledValue();

    double power = axis.getFormatter().getUnit().getFactor();
    final double minorTick = axis.getMinorTickSpacing() * power;
    final double majorTick = axis.getMajorTickSpacing() * power;

    double majorRound;

    if (floor) {
      majorRound = Math.floor(value / majorTick);
    } else {
      majorRound = Math.ceil(value / majorTick);
    }
    final boolean majorZeroHit = (majorRound == 0) && (value != 0);
    majorRound *= majorTick;
    double minorRound;
    if (floor) {
      minorRound = Math.floor(value / minorTick);
    } else {
      minorRound = Math.ceil(value / minorTick);
    }
    final boolean minorZeroHit = (minorRound == 0) && (value != 0);
    minorRound *= minorTick;
    if (majorZeroHit || minorZeroHit) {
      if (AAxis.DEBUG) {
        System.out.println("zeroHit");
      }
    }

    final double minorDistance = Math.abs(value - minorRound);
    final double majorDistance = Math.abs(value - majorRound);

    double majorMinorRelation = minorDistance / majorDistance;
    if (Double.isNaN(majorMinorRelation)) {
      majorMinorRelation = 1.0;
    }

    if ((majorDistance <= minorDistance) || findMajorTick) {
      ret.setValue(majorRound);
      ret.setMajorTick(true);
    } else {
      ret.setValue(minorRound);
      ret.setMajorTick(false);
    }

    // format label string.
    IAxisLabelFormatter formatter = axis.getFormatter();
    ret.setLabel(formatter.format(ret.getValue()));

    /*
     * As formatting rounds too, reparse value so that it is exactly at the
     * point the label string describes.
     * 
     * There are formatters that will loose value information by formatting.
     * E.g. a date formatter which does not render the year. So we use a
     * tolerance of 10 % between original value and formatted and reparsed
     * value.
     */

    double reparsed = formatter.parse(ret.getLabel()).doubleValue();
    double relativeDifferende = Math.abs(ret.getValue() - reparsed) / ret.getValue();
    if (relativeDifferende < 0.1) {
      ret.setValue(reparsed);
    } else {
      if (AAxis.DEBUG) {
        System.out.println("Axis formatter " + this.toString() + " looses information. Original value: " + ret.getValue() + ". Formatted value: "
            + ret.getLabel() + ". Reparsed value (dropped): " + reparsed);
      }
    }
    return ret;
  }

  public static void main(String[] args) {
    AxisScalePolicyAutomaticBestFit test = new AxisScalePolicyAutomaticBestFit();

    double a = test.glueToTicks(1, 10, 3);
    a = 10 % 100;
    a = test.glueToTicks(1, 10, 2.1);
    a = test.glueToTicks(1, 10, 99);
    a = test.glueToTicks(1, 3, 88);
    a = test.glueToTicks(1, 10, 9.046997389033942);
  }

}
