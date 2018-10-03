ability(A1,C) :- 	nonvar(A1),nonvar(A2),nonvar(C),	isa(A1,A2),ability(A2,C).
ability(A2,C) :- 	nonvar(A2),nonvar(A1),nonvar(C),	pw(A1,A2),ability(A1,C).
purpose(B,C) :- 	nonvar(A),nonvar(B),nonvar(C),		pw(A,B),purpose(A,C).
purpose(C,B) :- 	nonvar(A),nonvar(B),nonvar(C),		isa(C,A),purpose(A,B).
pw(A,C) :- 			nonvar(A),nonvar(B),nonvar(C),		pw(A,B),pw(B,C).
pw(A,C) :- 			nonvar(A),nonvar(B),nonvar(C),		pw(A,B),isa(C,B).
sound(B,C) :-		nonvar(A),nonvar(B),nonvar(C),		pw(A,B),sound(A,C).
isa(A,C) :- 		nonvar(A),nonvar(B),nonvar(C),		isa(A,B),isa(B,C).

pw('generic/leg',P) :- pw('horse/leg',P).
pw('generic/hoof',P) :- pw('horse/hoof',P).
pw('generic/snout',P) :- pw('horse/snout',P).
pw('generic/mane',P) :- pw('horse/mane',P).
pw('generic/tail',P) :- pw('horse/tail',P).
pw('generic/eye',P) :- pw('horse/eye',P).
pw('generic/ear',P) :- pw('horse/ear',P).
pw('generic/mouth',P) :- pw('horse/mouth',P).
pw('generic/lung',P) :- pw('bird/lung',P).
pw('generic/wing',P) :- pw('bird/wing',P).
pw('generic/feathers',P) :- pw('bird/feathers',P).
pw('generic/beak',P) :- pw('bird/beak',P).
pw('generic/straw',P) :- pw('bird/straw',P).
pw('generic/eye',P) :- pw('bird/eye',P).
pw('generic/leg',P) :- pw('bird/leg',P).
pw('generic/claw',P) :- pw('bird/claw',P).
ability(A,'generic/run') :- ability(A,'horse/run').
ability(A,'generic/run') :- ability(A,'bird/run').
ability(A,'generic/fly') :- ability(A,'horse/fly').
ability(A,'generic/fly') :- ability(A,'bird/fly').
