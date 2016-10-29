(* TODO handle gaps in strings -> we need alignment lengths! *)
(* TODO there won't be strings but fragment references *)

(*
Not a real issue since we reorder markers:
This is a nasty
     is a nasty ex
  is is a nasty example.
*)
(*
Hello wor
3  lo world, thi
|  11   |     his is a lo
|  |    |     7      a long sentence.
|  |    |     |      2 ling sente   |
|  |    |     |      | ||       |   |
0  1    0     21     3 42       4   3
<  <    >     <>     < <>       >   >
*)

let alignments = [
    ("Hello wor", 3, "lo world, thi");
    ("lo world, thi", 11, "his is a lo");
    ("his is a lo", 7, "a long sentence.");
    ("a long sentence.", 2, "ling sente");
]

let expected = "Hello world, this is a long sentence."

type marker = {start: bool; absolute: int; fragment: int}

(* Generate start and stop markers *)
let get_markers = function
| ((s, _, _) :: tail) as alignments ->
    let rec f i abs = (function
    | (_, delta, t) :: tail ->
        let p_start = abs + delta in
        let p_end = p_start + String.length t - 1 in
        {start=true; absolute=p_start; fragment=i} ::
        {start=false; absolute=p_end; fragment=i} ::
        f (i+1) p_start tail
    | [] -> []) in
    {start=true; absolute=0; fragment=0} ::
    {start=false; absolute=String.length s - 1; fragment=0} ::
    f 1 0 alignments
| [] -> []

let rec print_list = function
| head :: tail -> print_endline head; print_list tail
| [] -> ()

let compare_markers a b = a.absolute - b.absolute

let consensus alignments =
    let markers = List.sort compare_markers (get_markers alignments) in
    let fragments = match alignments with
    | (s, _, _) :: _ -> s :: List.map (fun (_, _, t) -> t) alignments
    | [] -> [] in
    (*
     * position is the absolute position in the contig.
     * processed is the number of fully-processed fragments,
     * the list in parameter contains the remaining fragments.
     *)
    (* FIXME don't use a list for started fragments but a counter! *)
    let rec f position processed started fragments = function
    | {start=s; absolute=a; fragment=i} :: tail ->
        if s then
            (*
             * merge the started fragments up to a (exclusive)
             * add i to the started list (with a zero local offset)
             *)
            "start " ^ (f position processed started fragments tail)
        else
            (*
             * merge the started fragments up to a (inclusive)
             * remove i from the started list
             * add position-a+1 to the started fragment offsets
             * increment position, processed
             *)
            "stop " ^ (f position processed started fragments tail)
    | [] -> "" in
    f 0 0 [] fragments markers

let () =
    print_string (consensus alignments)
