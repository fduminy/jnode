#!/bin/sh

qemu-system-x86_64 -serial file:logs.txt -machine accel=kvm:tcg -m 768 -display sdl -name "JNode x86" -cdrom all/build/cdroms/jnode-x86-lite.iso -usb -vga vmware $JNODE_QEMU_ARGS "$@"

