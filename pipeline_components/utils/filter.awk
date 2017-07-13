# Requires -v FIELD=xxx

BEGIN{
  OFS =FS
  split("",identifiers,",")
}
{
  if(FNR == NR) {
    identifiers[$0] = "1"
    print "adding " $0
  } else {
    if ( $FIELD in identifiers ){
      print $0
    }
  }
}
