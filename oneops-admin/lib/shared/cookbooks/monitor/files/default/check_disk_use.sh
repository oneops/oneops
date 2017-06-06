#!/bin/bash

space_used=`df -Pk $1 | tail -1 | awk '{print $5}'`
inode_used=`df -Pi $1 | tail -1 | awk '{print $5}'`

echo "$1 space used:$space_used inode used:$inode_used|space_used=$space_used inode_used=$inode_used"
