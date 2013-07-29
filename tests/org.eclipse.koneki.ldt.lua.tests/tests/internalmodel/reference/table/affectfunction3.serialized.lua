do local _={
		unknownglobalvars={

		}
		--[[table: 0x95de798]],
		content={
			localvars={
				[1]={
					item={
						type={
							def={
								shortdescription="",
								name="___",
								fields={
									functionname={
										type={
											def={
												shortdescription="",
												description="",
												name="___",
												returns={

												}
												--[[table: 0x942e250]],
												params={

												}
												--[[table: 0x942e1b8]],
												tag="functiontypedef"
											}
											--[[table: 0x943d958]],
											tag="inlinetyperef"
										}
										--[[table: 0x942e2d0]],
										description="",
										parent=nil --[[ref]],
										shortdescription="",
										name="functionname",
										sourcerange={
											min=33,
											max=44
										}
										--[[table: 0x9709ff8]],
										occurrences={

										}
										--[[table: 0x98d8f40]],
										tag="item"
									}
									--[[table: 0x98d8f18]]
								}
								--[[table: 0x943d8e0]],
								sourcerange={
									min=0,
									max=0
								}
								--[[table: 0x943d908]],
								description="",
								tag="recordtypedef"
							}
							--[[table: 0x943d8b8]],
							tag="inlinetyperef"
						}
						--[[table: 0x943d930]],
						description="",
						shortdescription="",
						name="tablename",
						sourcerange={
							min=7,
							max=15
						}
						--[[table: 0x95be018]],
						occurrences={
							[1]={
								sourcerange={
									min=7,
									max=15
								}
								--[[table: 0x98c7e18]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x95b9808]],
							[2]={
								sourcerange={
									min=23,
									max=31
								}
								--[[table: 0x94bc5f8]],
								definition=nil --[[ref]],
								tag="MIdentifier"
							}
							--[[table: 0x94bc518]]
						}
						--[[table: 0x95bdff0]],
						tag="item"
					}
					--[[table: 0x98763f8]],
					scope={
						min=0,
						max=0
					}
					--[[table: 0x970a140]]
				}
				--[[table: 0x970a0b8]]
			}
			--[[table: 0x9889500]],
			sourcerange={
				min=1,
				max=10000
			}
			--[[table: 0x9889528]],
			content={
				[1]=nil --[[ref]],
				[2]={
					sourcerange={
						min=23,
						max=44
					}
					--[[table: 0x95571e0]],
					right="functionname",
					left=nil --[[ref]],
					tag="MIndex"
				}
				--[[table: 0x9557178]],
				[3]={
					localvars={

					}
					--[[table: 0x9436270]],
					sourcerange={
						min=48,
						max=75
					}
					--[[table: 0x9436298]],
					content={

					}
					--[[table: 0x9436248]],
					tag="MBlock"
				}
				--[[table: 0x981c848]]
			}
			--[[table: 0x98894d8]],
			tag="MBlock"
		}
		--[[table: 0x95de7c0]],
		tag="MInternalContent"
	}
	--[[table: 0x95de710]];
	_.content.localvars[1].item.type.def.fields.functionname.parent=_.content.localvars[1].item.type.def;
	_.content.localvars[1].item.occurrences[1].definition=_.content.localvars[1].item;
	_.content.localvars[1].item.occurrences[2].definition=_.content.localvars[1].item;
	_.content.content[1]=_.content.localvars[1].item.occurrences[1];
	_.content.content[2].left=_.content.localvars[1].item.occurrences[2];
	return _;
end