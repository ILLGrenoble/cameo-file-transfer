# cameo-file-transfer

Test the file transfer:

```
$ cmo exec testfiletransfer write text /tmp/LSCO.cif LSCO.cif
$ cmo exec testfiletransfer read text /tmp/LSCO.cif
$ cmo exec testfiletransfer delete /tmp/LSCO.cif
```