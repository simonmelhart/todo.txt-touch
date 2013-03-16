#! /usr/bin/env python

# Copy this script to lic_header.py and edit the TODOTXTOUCH_HOME variable appropriately.

# This script updates the license/copyright information for all .java files (except those in the 'excludedir' list)
# which do not have license information added already.
# The script will indicate which files were modified, so that you can check them.
# This script is intended to be used either in standalone mode, or as a git pre-commit hook.
# ...
#   #!/bin/sh
#   ./extras/scripts/lic_header.py


import os, getopt, sys
import subprocess 
import shutil

#change this to the full path to your ThinkUp installation. E.g., 
# "/home/username/yourpathto/ThinkUp" or 
# "C:/yourpathto/ThinkUp"
TODOTXTOUCH_HOME = '/Users/gina/Documents/data/code/todo.txt-touch'

excludedir = ["/assets", "/.git", "/bin", "/extras", "/gen", "/libs", "/tests/res"]
ignorefns = ["default.properties"]
license_line = "http://www.gnu.org/licenses/gpl.html"
header_fname = "/extras/scripts/header_content_xml.txt"

verbose = False
quiet = False
backup = False
suffix = ".xml"
made_mods = False

help_message = '''
Usage: lic_header.py [options]
  -b               When modifying files, save "~" backup of original
  -v               Verbose output
  -q               Suppress all output
  -l <file>        Specify license header content
  --ltext <file>   Specify license header content
  --help           This help
'''


def update_source(filename, shortfn, copyright):
  global backup
  global made_mods
  global ignorefns
  
  if (shortfn in ignorefns):
    return
  
  utfstr = chr(0xef)+chr(0xbb)+chr(0xbf)
  fdata = file(filename,"r+").read()
  # arghh
  has_cp = (fdata.find("LICENSE") > 0) and (fdata.find("This file is part of Todo.txt Touch") > 0)
  # print "has_cp: %s" % has_cp
  if not has_cp:
    made_mods = True
    if not quiet:
      print >> sys.stderr, "updating "+filename
    isUTF = False
    nl = get_copyright_namelist(filename, False)
    filename_str = filename.replace(TODOTXTOUCH_HOME, "Todo.txt Touch")
    copyright_str = "" + filename_str + "\n\nCopyright (c) 2009-2013 " + nl +"\n"

    javaHeader = ""
    if (fdata.startswith(utfstr)):
      isUTF = True
      fdata = fdata[0:]
    if (fdata.startswith("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")):
      fdata = fdata[38:]
      #javaHeader = "package com.todotxt.todotxttouch;\n"

    tempfn = "/tmp" + shortfn
    nla = get_copyright_namelist(filename, True)
    nlc = get_copyright_namelist(filename, False)
    fdata = javaHeader + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n\n<!--\n\n" + copyright_str + copyright + build_docblock(nla, nlc) + fdata
    if (isUTF):
      file(tempfn,"w").write(utfstr+fdata)
    else:
      file(tempfn,"w").write(fdata)
      if backup:
        #os.rename(filename, filename+"~") can cause "Invalid cross-device link using os.rename error in Cygwin
        shutil.move(filename, filename+"~")
      #os.rename(tempfn, filename)
      shutil.move(tempfn, filename)

def recursive_traversal(dir, copyright):
    # print "in recursive_traversal"
    global excludedir
    global suffix
    global verbose
    
    fns = os.listdir(dir)
    if verbose:
      print "listing "+dir
    for fn in fns:
        fullfn = os.path.join(dir,fn)
        # if verbose:
          # print "got filename: " + fullfn
        if (fullfn in [TODOTXTOUCH_HOME+subdir for subdir in excludedir]):
            if not quiet:
              print >> sys.stderr, "Excluding: %s" % fullfn
            continue
        if (os.path.isdir(fullfn)):
            recursive_traversal(fullfn, copyright)
        else:
            if (fullfn.endswith(suffix)):
                if verbose:
                  print "checking whether source needs updating:" + fullfn
                update_source(fullfn, fn, copyright)


def build_docblock(nla, nlc):
  if nla is not "":
    nla += "\n"
  docblock = ("%s" + build_docblock_lic_copy(nlc) +" -->\n") % (nla,)
  return docblock
    
def build_docblock_lic_copy(nl):
  global license_line
  return "@license %s\n@copyright 2009-2013 %s\n" % (license_line, nl)

def get_copyright_namelist(filename, emailsp):

  #run a git command 
  if(emailsp):
      sub = subprocess.Popen(['git', 'shortlog', '-se', '--numbered', 'HEAD', filename],
        stdout=subprocess.PIPE)
  else:
      sub = subprocess.Popen(['git', 'shortlog', '-s', '--numbered', 'HEAD', filename],
        stdout=subprocess.PIPE)
  sub.wait()
  oput = sub.stdout.readlines()
      
  # print "output: %s" % oput
  clist= [numstrip(i, emailsp) for i in oput]
  if len(clist) > 0:
    if (emailsp):
        return '\n'.join(clist)
    else:
        return ', '.join(clist)
  else:
    return ""

def numstrip(line, emailsp):
    nl = line.strip().split('\t')[1]
    if (emailsp):
        nl = nl.replace("@", "[at]")
        nl = nl.replace(".", "[dot]")
        nl = "@author " + nl
    return nl


class Usage(Exception):
  def __init__(self, msg):
    self.msg = msg


def main(argv=None):
    
    global verbose
    global backup
    global header_fname
    global TODOTXTOUCH_HOME

    if TODOTXTOUCH_HOME is None:
      print >> sys.stderr, "Edit this script to set TODOTXTOUCH_HOME"
      return 2

    hfname = TODOTXTOUCH_HOME + header_fname

    if argv is None:
        argv = sys.argv

    try:
        try:
            opts, args = getopt.getopt(argv[1:], "l:vqb", ["help", "ltext="])
        except getopt.error, msg:
            raise Usage(msg)
    
        # option processing
        for option, value in opts:
            # print "option %s, value %s" % (option, value)
            if option == "-v":
                verbose = True
                print "setting verbose to true"
            if option == "-q":
                 quiet = True
                 print "setting quiet to true"
            if option == "-b":
                backup = True            
                print "setting backup to true"
            if option in ("-h", "--help"):
                raise Usage(help_message)
            if option in ("-l", "--ltext"):
              hfname = value

        if verbose:
          print "ThinkUp directory: %s" % (TODOTXTOUCH_HOME,)
          print "header file name: %s" % hfname

        cright = file(hfname,"r+").read()
        recursive_traversal(TODOTXTOUCH_HOME, cright)
        if made_mods:
          return 1
        else:
          return 0
        
    except Usage, err:
        print >> sys.stderr, sys.argv[0].split("/")[-1] + ": " + str(err.msg)
        print >> sys.stderr, "\t for help use --help"
        return 2


if __name__ == "__main__":
    sys.exit(main())

