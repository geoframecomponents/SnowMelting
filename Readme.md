<<<<<<< HEAD
# SnowMelting

This repository contains the SnowMelting component of the GEOframe modeling framework, implemented in Java. It provides various conceptual models for simulating snow dynamics, primarily focusing on Snow Water Equivalent (SWE) and snowmelt processes.


## Point-Case

The core models are implemented within the **it.geoframe.blogspot.snowmelting.pointcase** package and are designed for single-point simulations.

This component currently includes three primary snowmelt models:

   - **SnowMeltingPointCaseCazorzi.java**: Implements the Cazorzi snowmelt model (Specifics TODO: Add details on the Cazorzi model formulation).

   - **SnowMeltingPointCaseDegreeDay.java**: Implements the Temperature Index (Degree-Day) method.

  - **SnowMeltingPointCaseHock.java**: Implements the Hock snowmelt model (Specifics TODO: Add details on the Hock model formulation).

## Raster-Case Implementation

TODO: Tdocumentations for classes for raster-based ( in **it.geoframe.blogspot.snowmelting.rastercase**), .

---


## Mathematical Formulation: Temperature Index Method
The Degree-Day model (*SnowMeltingPointCaseDegreeDay.java*) is based on the classical temperature index method, as described in **Formetta et al. (2014)**.

=======
>>>>>>> build/add-maven-pom
**Snowmelt and freezing model** (temperature index method; Formetta et al., 2014)

$$
\begin{aligned}
L_{\max}(t) &= \alpha_l\, S_w(t) \\
\frac{dS_w(t)}{dt} &= P_s(t) + F(t) - M(t) \\
\frac{dL_w(t)}{dt} &= P_r(t) - F(t) + M(t)
\end{aligned}
$$


**Solid and liquid water dynamics**

$$
\begin{aligned}
M_d(t) &= \max\left(0,\; L(t) - L_{\max}(t)\right) \\
L_w(t) &= \min\left(L(t),\; L_{\max}(t)\right) \\
SWE(t) &= S_w(t) + L_w(t)
\end{aligned}
$$


## Snow Water Equivalent — Symbols Table

| Symbol        | Meaning                                   | Dimension        |
|---------------|--------------------------------------------|-------------|
| $$P(t)$$        | Precipitation at time *t*                  | [L·T⁻¹]      |
| $$P_r(t)$$      | Rainfall at time *t*                       | [L·T⁻¹]      |
| $$P_s(t)$$      | Snowfall at time *t*                       | [L·T⁻¹]      |
| $$T$$           | Temperature                                | [Θ]      |
| $$T_s$$         | Threshold air temperature                  | [Θ]          |
| $$α_m$$         | Melt factor                                | [L·T⁻¹·Θ⁻¹] |
| $$α_f$$         | Freezing factor                            | [L·T⁻¹·Θ⁻¹] |
| $$α_l$$         | Coefficient for maximum liquid water       | –           |
| $$M(t)$$        | Melt rate at time *t*                      | [L·T⁻¹]      |
| $$F(t)$$        | Freezing rate at time *t*                  | [L·T⁻¹]      |
| $$S_w(t)$$      | Solid water                                | [L]          |
| $$L_w(t)$$      | Liquid water                               | [L]          |
| $$L_max(t)$$    | Maximum liquid water value                 | [L]          |
| $$SWE(t)$$      | Snow Water Equivalent                      | [L]          |

| Symbol | Meaning                   | Examples                          |
| ------ | ------------------------- | --------------------------------- |
| **L**  | Length                    | mm, m, km, water depth, elevation |
| **T**  | Time                      | seconds, hours, days              |
| **M**  | Mass                      | kg                                |
| **Θ**  | Thermodynamic temperature | °C, K                             |
| **–**  | Dimensionless quantity    | coefficients, ratios, fractions   |

<<<<<<< HEAD
---
## References

TODO: add all references
=======

>>>>>>> build/add-maven-pom

Formetta, G., Kampf, S. K., David, O., & Rigon, R. (2014, 6 May). Snow water
equivalent modeling components in NewAge-JGrass. Geosci. Model Dev., 7 (3),
725–736.
