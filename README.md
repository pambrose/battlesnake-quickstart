# Battlesnake framework for creating Kotlin and Java Snakes

[![Build Status](https://travis-ci.org/pambrose/battlesnake-dispatch.svg?branch=master)](https://travis-ci.org/pambrose/simple-battlesnake)
[![Release](https://jitpack.io/v/pambrose/battlesnake-dispatch.svg)](https://jitpack.io/#pambrose/battlesnake-dispatch)

Out of the box, [BattleSnake](https://battlsnake.io) requires a fair amount of JSON/REST wiring before one 
can start authoring a snake. That initial exercise can prove problematic for some. This repo takes care of 
the wiring and provides a simple framework for writing snakes.  

A snake is a subclass of AbstractBattleSnake and requires a GameContext and a Strategy. A GameContext is created at 
the start of every game and provides context between turns. A Strategy specifies actions for Ping, Start, Move, and End.

A minimal Kotlin snake is here and a minimal Java snake is here.


