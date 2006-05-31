;;;
;;; CLTL2 lisp script, prints out a Mathematica equivalent to a GP tree,
;;; for testing purposes (Show[f[x],{x,-1,1} is useful)
;;;

(defun conv (exp)
  (cond 
   ((not (listp exp)) "x")
   ((equal (first exp) '+)
    (concatenate 'string "Plus[" (conv (second exp)) "," (conv (third exp)) "]"))
   ((equal (first exp) '-) 
    (concatenate 'string "Subtract[" (conv (second exp)) "," (conv (third exp)) "]"))
   ((equal (first exp) '*) 
    (concatenate 'string "Times[" (conv (second exp)) "," (conv (third exp)) "]"))
   ((equal (first exp) '%) 
    (concatenate 'string "PDivide[" (conv (second exp)) "," (conv (third exp)) "]"))
   ((equal (first exp) 'exp) 
    (concatenate 'string "Exp[" (conv (second exp)) "]"))
   ((equal (first exp) 'sin) 
    (concatenate 'string "Sin[" (conv (second exp))"]"))
   ((equal (first exp) 'cos) 
    (concatenate 'string "Cos[" (conv (second exp))"]"))
   ((equal (first exp) 'rlog) 
    (concatenate 'string "RLog[" (conv (second exp))"]"))))

(print "PDivide[x_,y_] := If[y==0,1,x/y]")
(print "RLog[x_] := If[x==0,1,Log[x]]")

(princ (conv '(+ (sin (sin (+ (+ x x) (+ x x)))) (% (rlog
     (* (* x x) (exp x))) (% (* (exp x) (rlog
     x)) (sin (+ x x)))))))

(conv '(+ x x))
(conv '(sin x))

(conv ' (+ (sin x) (* (+ (* (* (cos (rlog x)) x)
     (+ (* (cos (rlog x)) x) (% x (cos (rlog x)))))
     (% x (% x x))) x)))
