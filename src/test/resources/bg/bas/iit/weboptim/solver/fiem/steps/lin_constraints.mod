param a := 3;

var x1 integer >= 0 <= 1000;
var x2 integer >= 0 <= 100;
var x3 integer >= 0 <= 100;
var x4 integer >= 0 <= 100;

/* Критерии */
minimize f1: 1/(x1 + 1);
minimize f2: 1/(x2 + 1);

c1: x1 + 10*x2 - 12*x3 <= 10^6;
c2: (3 + a) * x1 + (2^2)* x2 - x3 * (4^3) + x4 - x4>= 14;
