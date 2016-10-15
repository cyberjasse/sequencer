open Printf

(* Computes the similarity matrix between s and t, and an optimal alignment *)
(* TODO generalize to objects (lines, for example) instead of characters! *)
let similarity s t gap_s match_s mismatch_s =
    let m = String.length s in
    let n = String.length t in
    let a = Array.make_matrix (m+1) (n+1) 0 in
    (* initialize the first column *)
    for i = 0 to m do
        a.(i).(0) <- i * gap_s
    done;
    (* initialize the first row *)
    for j = 0 to n do
        a.(0).(j) <- j * gap_s
    done;
    (* this computes the arguments for the main loop's max, with i,j>0 *)
    let neighbors i j =
        let delta =
            let c = String.get s (i-1) in
            let d = String.get t (j-1) in
            if c = d then match_s else mismatch_s in
        a.(i).(j-1) + gap_s, (* right *)
        a.(i-1).(j) + gap_s, (* down *)
        a.(i-1).(j-1) + delta (* diagonal *) in
    (* main loop *)
    for i = 1 to m do
        for j = 1 to n do
            let right, down, diagonal = neighbors i j in
            a.(i).(j) <- max (max right down) diagonal
        done;
    done;
    (* generate an optimal alignment from a *)
    let rec align i j p q =
        if i=0 && j=0 then
            (* base case: we're done! *)
            p, q
        else if i=0 then
            (* we have to get closer to (0,0) going left *)
            align i (j-1) ("-"^p) ((String.sub t (j-1) 1) ^ q)
        else if j=0 then
            (* we have to get closer to (0,0) going up *)
            align (i-1) j ((String.sub s (i-1) 1) ^ p) ("-"^q)
        else
            (* where does this score come from? (3 possible answers) *)
            let left, up, diagonal = neighbors i j in
            (* both strings can now be pre-computed *)
            (* TODO use lazy values sooner? *)
            let before_s = (String.sub s (i-1) 1) ^ p in
            let before_t = (String.sub t (j-1) 1) ^ q in
            (* TODO generate all possibilities? *)
            if a.(i).(j) = left then
                align i (j-1) ("-"^p) before_t
            else if a.(i).(j) = up then
                align (i-1) j before_s ("-"^q)
            else (* diagonal *)
                align (i-1) (j-1) before_s before_t
        in
    a, align m n "" ""


(* Prints a 5-digit integer array *)
let print_array a =
    let m = Array.length a in
    let n = Array.length a.(0) in
    for i = 0 to m-1 do
        for j = 0 to n-1 do
            printf "%5d " a.(i).(j)
        done;
        print_newline ()
    done

let () =
    let n_args = Array.length Sys.argv in
    if n_args=3 || n_args=6 then
        let s, t = Sys.argv.(1), Sys.argv.(2) in
        let a, (p, q) =
            if n_args = 3 then
                similarity s t (-2) 1 (-1)
            else
                let gap_s = int_of_string (Sys.argv.(3)) in
                let match_s = int_of_string (Sys.argv.(4)) in
                let mismatch_s = int_of_string (Sys.argv.(5)) in
                similarity s t gap_s match_s mismatch_s
            in
        print_array a;
        printf "score: %d\n" a.(String.length s).(String.length t);
        print_endline p;
        print_endline q;
    else
        eprintf "usage: %s s t [gap match mismatch]\n" Sys.argv.(0)
